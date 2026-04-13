package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.gtu.employeeperformancetracker.data.local.database.AppDatabase
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmployeeRepository

    // 🔥 Use StateFlow (better than LiveData for Compose)
    val employees = repositoryFlow(application)

    init {
        val dao = AppDatabase.getDatabase(application).employeeDao()
        repository = EmployeeRepository(dao)
    }

    private fun repositoryFlow(application: Application) =
        AppDatabase.getDatabase(application)
            .employeeDao()
            .getAllEmployees()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ Insert Employee
    fun insertEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insert(employee)
        }
    }

    // ✅ Simpler function for UI
    fun addEmployee(name: String, role: String, dept: String) {
        insertEmployee(
            Employee(
                name = name,
                role = role,
                department = dept,
                rating = 0f
            )
        )
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.delete(employee)
        }
    }
}
