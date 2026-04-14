package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_session")
data class AppSession(
    @PrimaryKey
    val id: Int = 1,
    val currentUserId: Int? = null
)
