package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.repository.AuthRepository
import com.gtu.employeeperformancetracker.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository()
    private val authRepository = AuthRepository(application)

    val employees: StateFlow<List<Employee>> = repository.getAllEmployees()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addEmployee(
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
            repository.insert(
                Employee(
                    employeeCode = employeeCode,
                    name = name,
                    role = role,
                    department = department,
                    joiningDate = joiningDate,
                    email = email,
                    contact = contact,
                    profilePictureUri = profilePictureUri?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.update(employee)
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            authRepository.deleteEmployeeAccount(employee.id)
            repository.delete(employee)
        }
    }
}
