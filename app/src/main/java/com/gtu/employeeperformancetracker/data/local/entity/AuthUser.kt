package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auth_users",
    indices = [Index(value = ["email"], unique = true)]
)
data class AuthUser(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int? = null,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    val email: String,
    val password: String,
    val role: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "force_password_reset")
    val forcePasswordReset: Boolean = false,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
