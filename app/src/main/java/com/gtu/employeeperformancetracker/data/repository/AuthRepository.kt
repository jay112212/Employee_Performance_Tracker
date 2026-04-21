package com.gtu.employeeperformancetracker.data.repository

import android.content.Context
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.gtu.employeeperformancetracker.utils.DefaultAccounts
import com.gtu.employeeperformancetracker.utils.LoginModes
import com.gtu.employeeperformancetracker.utils.Roles
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val store: FirebaseStore = FirebaseStore(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val firestore = store.firestore()

    fun observeUsers(): Flow<List<AuthUser>> = store.observeCollection(
        firestore.collection(AUTH_USERS_COLLECTION)
    ) { it.toAuthUser() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCurrentUser(): Flow<AuthUser?> = observeFirebaseSession()
        .flatMapLatest { firebaseUser ->
            if (firebaseUser == null) {
                flowOf(null)
            } else {
                store.observeDocument(
                    firestore.collection(AUTH_USERS_COLLECTION).document(firebaseUser.uid)
                ) { snapshot ->
                    snapshot.toAuthUser() ?: fallbackManagementUser(firebaseUser)
                }
            }
        }

    suspend fun getUserByEmail(email: String): AuthUser? {
        val snapshot = firestore.collection(AUTH_USERS_COLLECTION)
            .whereEqualTo("email", email.trim())
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toAuthUser()
    }

    suspend fun login(email: String, password: String, mode: String): AuthUser {
        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
            val firebaseUser = result.user ?: error("Unable to load Firebase session.")
            val user = loadUserProfile(firebaseUser)

            if (!user.isActive) {
                auth.signOut()
                error("This employee account has been deactivated.")
            }

            if (!matchesMode(user.role, mode)) {
                auth.signOut()
                error("This account does not match the selected login mode.")
            }

            return user
        } catch (error: FirebaseAuthInvalidUserException) {
            throw IllegalStateException("No Firebase account exists for this email.", error)
        } catch (error: FirebaseAuthInvalidCredentialsException) {
            throw IllegalStateException("Invalid email or password.", error)
        } catch (error: FirebaseAuthException) {
            throw IllegalStateException(error.localizedMessage ?: "Unable to sign in.", error)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun updateUser(user: AuthUser) {
        firestore.collection(AUTH_USERS_COLLECTION)
            .document(user.uid)
            .set(user.toMap())
            .await()
    }

    suspend fun changePassword(newPassword: String) {
        val currentUser = auth.currentUser ?: error("No active Firebase session found.")
        currentUser.updatePassword(newPassword).await()
    }

    suspend fun ensureDefaultManagementAccounts() {
        ensureManagementAccount(
            email = DefaultAccounts.ADMIN_EMAIL,
            password = DefaultAccounts.ADMIN_PASSWORD,
            displayName = "System Admin",
            role = Roles.ADMIN
        )
        ensureManagementAccount(
            email = DefaultAccounts.HR_EMAIL,
            password = DefaultAccounts.HR_PASSWORD,
            displayName = "HR Manager",
            role = Roles.HR
        )
    }

    suspend fun createEmployeeAccount(
        employeeId: Int,
        displayName: String,
        email: String,
        temporaryPassword: String
    ): AuthUser {
        if (getUserByEmail(email) != null) {
            error("A login account already exists for ${email.trim()}.")
        }

        val createdUser = createSecondaryFirebaseUser(email, temporaryPassword)
        val authUserId = store.nextId(AUTH_COUNTER_KEY)

        val authUser = AuthUser(
            id = authUserId,
            uid = createdUser.uid,
            employeeId = employeeId,
            displayName = displayName.trim(),
            email = email.trim(),
            password = "",
            role = Roles.EMPLOYEE,
            createdAt = LocalDateTime.now().toString(),
            forcePasswordReset = true,
            isActive = true
        )

        try {
            firestore.collection(AUTH_USERS_COLLECTION)
                .document(authUser.uid)
                .set(authUser.toMap())
                .await()
        } catch (error: Exception) {
            deleteSecondaryFirebaseUser(createdUser.email, temporaryPassword)
            throw error
        }

        return authUser
    }

    suspend fun deleteEmployeeAccount(employeeId: Int) {
        val user = firestore.collection(AUTH_USERS_COLLECTION)
            .whereEqualTo("employeeId", employeeId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toAuthUser()
            ?: return

        firestore.collection(AUTH_USERS_COLLECTION)
            .document(user.uid)
            .set(user.copy(isActive = false).toMap())
            .await()
    }

    private fun observeFirebaseSession(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            trySend(authState.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private suspend fun ensureManagementAccount(
        email: String,
        password: String,
        displayName: String,
        role: String
    ) {
        runCatching { getUserByEmail(email) }
            .getOrNull()
            ?.let { return }

        val createdUser = createSecondaryFirebaseUser(email, password)
        val authUserId = store.nextId(AUTH_COUNTER_KEY)
        val authUser = AuthUser(
            id = authUserId,
            uid = createdUser.uid,
            employeeId = null,
            displayName = displayName,
            email = email,
            password = "",
            role = role,
            createdAt = LocalDateTime.now().toString(),
            forcePasswordReset = false,
            isActive = true
        )

        firestore.collection(AUTH_USERS_COLLECTION)
            .document(authUser.uid)
            .set(authUser.toMap())
            .await()
    }

    private suspend fun createSecondaryFirebaseUser(
        email: String,
        password: String
    ): FirebaseUser {
        val primaryApp = FirebaseApp.getInstance()
        val secondaryAppName = "secondary-${UUID.randomUUID()}"
        val secondaryApp = FirebaseApp.initializeApp(
            context,
            primaryApp.options,
            secondaryAppName
        ) ?: error("Unable to initialize secondary Firebase app.")

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        return try {
            val result = secondaryAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            result.user ?: error("Unable to create Firebase account.")
        } finally {
            secondaryAuth.signOut()
            secondaryApp.delete()
        }
    }

    private suspend fun deleteSecondaryFirebaseUser(email: String?, password: String) {
        if (email.isNullOrBlank()) return

        val primaryApp = FirebaseApp.getInstance()
        val secondaryAppName = "cleanup-${UUID.randomUUID()}"
        val secondaryApp = FirebaseApp.initializeApp(
            context,
            primaryApp.options,
            secondaryAppName
        ) ?: return

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        try {
            val result = secondaryAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.delete()?.await()
        } finally {
            secondaryAuth.signOut()
            secondaryApp.delete()
        }
    }

    private fun matchesMode(role: String, mode: String): Boolean {
        return when (mode) {
            LoginModes.ADMIN_HR -> role == Roles.ADMIN || role == Roles.HR
            LoginModes.EMPLOYEE -> role == Roles.EMPLOYEE
            else -> false
        }
    }

    private suspend fun loadUserProfile(firebaseUser: FirebaseUser): AuthUser {
        return try {
            val snapshot = firestore.collection(AUTH_USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()
            snapshot.toAuthUser()
                ?: fallbackManagementUser(firebaseUser)?.also { fallback ->
                    runCatching { upsertManagementFallback(firebaseUser, fallback) }
                }
                ?: run {
                    auth.signOut()
                    error("No account profile was found for this login.")
                }
        } catch (error: FirebaseFirestoreException) {
            fallbackManagementUser(firebaseUser) ?: throw IllegalStateException(
                error.localizedMessage ?: "Unable to load account profile from Firestore.",
                error
            )
        }
    }

    private suspend fun upsertManagementFallback(firebaseUser: FirebaseUser, fallbackUser: AuthUser) {
        firestore.collection(AUTH_USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(fallbackUser.toMap())
            .await()
    }

    private fun fallbackManagementUser(firebaseUser: FirebaseUser): AuthUser? {
        val email = firebaseUser.email ?: return null
        val now = LocalDateTime.now().toString()
        return when (email.lowercase()) {
            DefaultAccounts.ADMIN_EMAIL.lowercase() -> AuthUser(
                id = 1,
                uid = firebaseUser.uid,
                displayName = "System Admin",
                email = email,
                password = "",
                role = Roles.ADMIN,
                createdAt = now,
                forcePasswordReset = false,
                isActive = true
            )

            DefaultAccounts.HR_EMAIL.lowercase() -> AuthUser(
                id = 2,
                uid = firebaseUser.uid,
                displayName = "HR Manager",
                email = email,
                password = "",
                role = Roles.HR,
                createdAt = now,
                forcePasswordReset = false,
                isActive = true
            )

            else -> null
        }
    }

    private fun AuthUser.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "employeeId" to employeeId,
        "displayName" to displayName,
        "email" to email,
        "role" to role,
        "createdAt" to createdAt,
        "forcePasswordReset" to forcePasswordReset,
        "isActive" to isActive
    )

    private fun DocumentSnapshot.toAuthUser(): AuthUser? {
        val uid = getString("uid") ?: id
        val email = getString("email") ?: return null
        return AuthUser(
            id = (getLong("id") ?: 0L).toInt(),
            uid = uid,
            employeeId = getLong("employeeId")?.toInt(),
            displayName = getString("displayName").orEmpty(),
            email = email,
            password = "",
            role = getString("role").orEmpty(),
            createdAt = getString("createdAt").orEmpty(),
            forcePasswordReset = getBoolean("forcePasswordReset") ?: false,
            isActive = getBoolean("isActive") ?: true
        )
    }

    companion object {
        private const val AUTH_USERS_COLLECTION = "auth_users"
        private const val AUTH_COUNTER_KEY = "auth_users"
    }
}
