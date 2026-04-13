package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.EmployeeDao
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val dao: EmployeeDao) {

    suspend fun insert(employee: Employee) = dao.insertEmployee(employee)

    suspend fun update(employee: Employee) = dao.updateEmployee(employee)

    suspend fun delete(employee: Employee) = dao.deleteEmployee(employee)

    fun getAllEmployees(): Flow<List<Employee>> = dao.getAllEmployees()

    fun getEmployeeById(id: Int): Flow<Employee?> = dao.getEmployeeById(id)
}
