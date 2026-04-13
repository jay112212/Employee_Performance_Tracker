package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_code")
    val employeeCode: String,
    val name: String,
    val role: String,
    val department: String,
    @ColumnInfo(name = "joining_date")
    val joiningDate: String,
    val email: String,
    val contact: String,
    @ColumnInfo(name = "profile_picture_uri")
    val profilePictureUri: String? = null
)
