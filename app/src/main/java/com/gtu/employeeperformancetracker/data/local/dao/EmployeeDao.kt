package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.*
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    // 🔹 INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    // 🔹 GET ALL
    @Query("SELECT * FROM employees ORDER BY id DESC")
    fun getAllEmployees(): Flow<List<Employee>>

    // 🔹 DELETE
    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // 🔹 UPDATE
    @Update
    suspend fun updateEmployee(employee: Employee)

    // 🔹 GET BY ID
    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeById(id: Int): Flow<Employee>
}