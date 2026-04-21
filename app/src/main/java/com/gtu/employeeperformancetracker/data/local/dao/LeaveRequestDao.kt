package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest): Long

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequest)

    @Query("SELECT * FROM leave_requests ORDER BY applied_at DESC, id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employee_id = :employeeId ORDER BY applied_at DESC, id DESC")
    fun getLeaveRequestsByEmployee(employeeId: Int): Flow<List<LeaveRequest>>
}
