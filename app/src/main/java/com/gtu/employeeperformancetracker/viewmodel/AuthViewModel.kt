package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.database.AppDatabase
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.repository.AuthRepository
import com.gtu.employeeperformancetracker.data.repository.EmailRepository
import com.gtu.employeeperformancetracker.data.repository.EmployeeRepository
import com.gtu.employeeperformancetracker.utils.DefaultAccounts
import com.gtu.employeeperformancetracker.utils.LoginModes
import com.gtu.employeeperformancetracker.utils.Roles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val authRepository = AuthRepository(database.authUserDao(), database.sessionDao())
    private val employeeRepository = EmployeeRepository(database.employeeDao())
    private val emailRepository = EmailRepository(database.emailLogDao())

    val users: StateFlow<List<AuthUser>> = authRepository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val session = authRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val employees = employeeRepository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emailLogs = emailRepository.observeEmails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUser: StateFlow<AuthUser?> = combine(users, session) { usersList, currentSession ->
        usersList.find { it.id == currentSession?.currentUserId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentEmployee: StateFlow<Employee?> = combine(employees, currentUser) { employeeList, user ->
        employeeList.find { it.id == user?.employeeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _onboardingNotice = MutableStateFlow<String?>(null)
    val onboardingNotice: StateFlow<String?> = _onboardingNotice

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError

    init {
        viewModelScope.launch {
            seedDefaultAccounts()
        }
    }

    fun login(email: String, password: String, mode: String) {
        viewModelScope.launch {
            val user = authRepository.authenticate(email.trim(), password.trim())
            when {
                user == null -> _loginError.value = "Invalid email or password."
                !matchesMode(user.role, mode) -> {
                    _loginError.value = "This account does not match the selected login mode."
                }

                else -> {
                    authRepository.saveSession(user.id)
                    _loginError.value = null
                    _passwordChangeError.value = null
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.saveSession(null)
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearOnboardingNotice() {
        _onboardingNotice.value = null
    }

    fun clearPasswordChangeError() {
        _passwordChangeError.value = null
    }

    fun changePassword(
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            when {
                user == null -> _passwordChangeError.value = "No active user session found."
                newPassword.trim().length < 6 -> {
                    _passwordChangeError.value = "Password must be at least 6 characters long."
                }

                newPassword != confirmPassword -> {
                    _passwordChangeError.value = "New password and confirm password do not match."
                }

                else -> {
                    authRepository.updateUser(
                        user.copy(
                            password = newPassword.trim(),
                            forcePasswordReset = false
                        )
                    )
                    _passwordChangeError.value = null
                }
            }
        }
    }

    fun onboardEmployee(
        employeeCode: String,
        name: String,
        role: String,
        department: String,
        joiningDate: String,
        email: String,
        contact: String,
        profilePictureUri: String?
    ) {
        viewModelScope.launch {
            if (authRepository.getUserByEmail(email.trim()) != null) {
                _onboardingNotice.value = "A login account already exists for ${email.trim()}."
                return@launch
            }

            val employeeId = employeeRepository.insert(
                Employee(
                    employeeCode = employeeCode.trim(),
                    name = name.trim(),
                    role = role.trim(),
                    department = department.trim(),
                    joiningDate = joiningDate.trim(),
                    email = email.trim(),
                    contact = contact.trim(),
                    profilePictureUri = profilePictureUri?.trim()?.ifBlank { null }
                )
            ).toInt()

            val tempPassword = buildTempPassword(employeeCode.trim())

            authRepository.insertUser(
                AuthUser(
                    employeeId = employeeId,
                    displayName = name.trim(),
                    email = email.trim(),
                    password = tempPassword,
                    role = Roles.EMPLOYEE,
                    createdAt = LocalDateTime.now().toString()
                    ,
                    forcePasswordReset = true
                )
            )

            val emailBody = buildString {
                appendLine("Welcome to Employee Performance Tracker.")
                appendLine("Your account has been created by Admin / HR.")
                appendLine("Employee ID: ${employeeCode.trim()}")
                appendLine("Login email: ${email.trim()}")
                appendLine("Temporary password: $tempPassword")
                appendLine("Role: ${Roles.EMPLOYEE}")
                appendLine("Important: You must change your password immediately after your first login.")
            }

            emailRepository.insert(
                EmailLog(
                    recipientEmail = email.trim(),
                    recipientName = name.trim(),
                    subject = "Welcome to Employee Performance Tracker",
                    body = emailBody,
                    sentAt = LocalDateTime.now().toString()
                )
            )

            _onboardingNotice.value =
                "Employee onboarded successfully. Mock onboarding email created for ${email.trim()} with employee ID ${employeeCode.trim()} and temporary password $tempPassword."
        }
    }

    private suspend fun seedDefaultAccounts() {
        if (authRepository.getUserByEmail(DefaultAccounts.ADMIN_EMAIL) == null) {
            authRepository.insertUser(
                AuthUser(
                    displayName = "System Admin",
                    email = DefaultAccounts.ADMIN_EMAIL,
                    password = DefaultAccounts.ADMIN_PASSWORD,
                    role = Roles.ADMIN,
                    createdAt = LocalDateTime.now().toString()
                )
            )
        }

        if (authRepository.getUserByEmail(DefaultAccounts.HR_EMAIL) == null) {
            authRepository.insertUser(
                AuthUser(
                    displayName = "HR Manager",
                    email = DefaultAccounts.HR_EMAIL,
                    password = DefaultAccounts.HR_PASSWORD,
                    role = Roles.HR,
                    createdAt = LocalDateTime.now().toString()
                )
            )
        }
    }

    private fun matchesMode(role: String, mode: String): Boolean {
        return when (mode) {
            LoginModes.ADMIN_HR -> role == Roles.ADMIN || role == Roles.HR
            LoginModes.EMPLOYEE -> role == Roles.EMPLOYEE
            else -> false
        }
    }

    private fun buildTempPassword(employeeCode: String): String {
        return "EPT-${employeeCode.takeLast(4).ifBlank { "0000" }}!"
    }
}
