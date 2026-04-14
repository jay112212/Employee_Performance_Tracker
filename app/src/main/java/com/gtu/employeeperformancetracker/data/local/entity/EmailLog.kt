package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "email_logs")
data class EmailLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "recipient_email")
    val recipientEmail: String,
    @ColumnInfo(name = "recipient_name")
    val recipientName: String,
    val subject: String,
    val body: String,
    @ColumnInfo(name = "sent_at")
    val sentAt: String
)
