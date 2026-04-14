package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeById(id: Int): Flow<Employee?>

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)
}
