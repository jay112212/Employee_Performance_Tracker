package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.repository.AuthRepository
import com.gtu.employeeperformancetracker.data.repository.EmailRepository
import com.gtu.employeeperformancetracker.data.repository.EmployeeRepository
import com.gtu.employeeperformancetracker.utils.FirebaseErrorFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val employeeRepository = EmployeeRepository()
    private val emailRepository = EmailRepository()

    val users: StateFlow<List<AuthUser>> = authRepository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUser: StateFlow<AuthUser?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val employees: StateFlow<List<Employee>> = employeeRepository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emailLogs = emailRepository.observeEmails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentEmployee: StateFlow<Employee?> = combine(employees, currentUser) { employeeList, user ->
        employeeList.find { it.id == user?.employeeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _onboardingNotice = MutableStateFlow<String?>(null)
    val onboardingNotice: StateFlow<String?> = _onboardingNotice

    private val _onboardingError = MutableStateFlow(false)
    val onboardingError: StateFlow<Boolean> = _onboardingError

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError

    init {
        viewModelScope.launch {
            runCatching { authRepository.ensureDefaultManagementAccounts() }
                .onFailure {
                    _loginError.value = FirebaseErrorFormatter.format(
                        it.message,
                        "Unable to prepare management accounts."
                    )
                }
        }
    }

    fun login(email: String, password: String, mode: String) {
        viewModelScope.launch {
            runCatching {
                authRepository.login(email.trim(), password.trim(), mode)
            }.onSuccess {
                _loginError.value = null
                _passwordChangeError.value = null
            }.onFailure { error ->
                _loginError.value = FirebaseErrorFormatter.format(
                    error.message,
                    "Unable to sign in."
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearOnboardingNotice() {
        _onboardingNotice.value = null
        _onboardingError.value = false
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
                user == null -> _passwordChangeError.value = "No active Firebase session found."
                newPassword.trim().length < 6 -> {
                    _passwordChangeError.value = "Password must be at least 6 characters long."
                }

                newPassword != confirmPassword -> {
                    _passwordChangeError.value = "New password and confirm password do not match."
                }

                else -> {
                    runCatching {
                        authRepository.changePassword(newPassword.trim())
                        authRepository.updateUser(
                            user.copy(forcePasswordReset = false)
                        )
                    }.onSuccess {
                        _passwordChangeError.value = null
                    }.onFailure { error ->
                        _passwordChangeError.value = FirebaseErrorFormatter.format(
                            error.message,
                            "Unable to change password. Please login again and retry."
                        )
                    }
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
            runCatching {
                val normalizedEmail = email.trim()
                if (authRepository.getUserByEmail(normalizedEmail) != null) {
                    error("A login account already exists for $normalizedEmail.")
                }

                val employeeId = employeeRepository.insert(
                    Employee(
                        employeeCode = employeeCode.trim(),
                        name = name.trim(),
                        role = role.trim(),
                        department = department.trim(),
                        joiningDate = joiningDate.trim(),
                        email = normalizedEmail,
                        contact = contact.trim(),
                        profilePictureUri = profilePictureUri?.trim()?.ifBlank { null }
                    )
                ).toInt()

                val tempPassword = buildTempPassword(employeeCode.trim())

                try {
                    authRepository.createEmployeeAccount(
                        employeeId = employeeId,
                        displayName = name.trim(),
                        email = normalizedEmail,
                        temporaryPassword = tempPassword
                    )
                } catch (error: Exception) {
                    employeeRepository.delete(
                        Employee(
                            id = employeeId,
                            employeeCode = employeeCode.trim(),
                            name = name.trim(),
                            role = role.trim(),
                            department = department.trim(),
                            joiningDate = joiningDate.trim(),
                            email = normalizedEmail,
                            contact = contact.trim(),
                            profilePictureUri = profilePictureUri?.trim()?.ifBlank { null }
                        )
                    )
                    throw error
                }

                val emailBody = buildString {
                    appendLine("Welcome to Employee Performance Tracker.")
                    appendLine("Your account has been created by Admin / HR.")
                    appendLine("Employee ID: ${employeeCode.trim()}")
                    appendLine("Login email: $normalizedEmail")
                    appendLine("Temporary password: $tempPassword")
                    appendLine("Role: EMPLOYEE")
                    appendLine("Important: You must change your password immediately after your first login.")
                }

                emailRepository.insert(
                    EmailLog(
                        recipientEmail = normalizedEmail,
                        recipientName = name.trim(),
                        subject = "Welcome to Employee Performance Tracker",
                        body = emailBody,
                        sentAt = LocalDateTime.now().toString()
                    )
                )

                tempPassword
            }.onSuccess { tempPassword ->
                _onboardingError.value = false
                _onboardingNotice.value =
                    "Employee onboarded in Firebase successfully. An onboarding email was queued for ${email.trim()} with temporary password $tempPassword."
            }.onFailure { error ->
                _onboardingError.value = true
                _onboardingNotice.value = FirebaseErrorFormatter.format(
                    error.message,
                    "Unable to onboard employee."
                )
            }
        }
    }

    private fun buildTempPassword(employeeCode: String): String {
        return "EPT-${employeeCode.takeLast(4).ifBlank { "0000" }}!"
    }
}
