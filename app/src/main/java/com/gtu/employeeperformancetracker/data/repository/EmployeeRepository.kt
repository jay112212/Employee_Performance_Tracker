package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.EmployeeDao
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val dao: EmployeeDao) {

    // INSERT
    suspend fun insert(employee: Employee) {
        dao.insertEmployee(employee)
    }

    // GET ALL
    fun getAllEmployees(): Flow<List<Employee>> {
        return dao.getAllEmployees()
    }

    // DELETE ✅ ADD THIS
    suspend fun delete(employee: Employee) {
        dao.deleteEmployee(employee)
    }

    // UPDATE (optional for future)
    suspend fun update(employee: Employee) {
        dao.updateEmployee(employee)
    }
}