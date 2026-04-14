package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int,
    val date: String,
    val status: String,
    @ColumnInfo(name = "check_in_time")
    val checkInTime: String? = null,
    @ColumnInfo(name = "check_out_time")
    val checkOutTime: String? = null,
    val location: String? = null
)
