package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int,
    val description: String,
    val deadline: String,
    val priority: String,
    @ColumnInfo(name = "assigned_date")
    val assignedDate: String,
    val status: String
)
