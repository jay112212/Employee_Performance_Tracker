package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int,
    @ColumnInfo(name = "leave_type")
    val leaveType: String,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "end_date")
    val endDate: String,
    val reason: String,
    val status: String = "Pending",
    @ColumnInfo(name = "applied_at")
    val appliedAt: String
)
