package com.gtu.employeeperformancetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "performance_reviews")
data class Performance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Int,
    @ColumnInfo(name = "review_date")
    val reviewDate: String,
    @ColumnInfo(name = "quality_score")
    val qualityScore: Int,
    @ColumnInfo(name = "timeliness_score")
    val timelinessScore: Int,
    @ColumnInfo(name = "attendance_score")
    val attendanceScore: Int,
    @ColumnInfo(name = "communication_score")
    val communicationScore: Int,
    @ColumnInfo(name = "innovation_score")
    val innovationScore: Int,
    @ColumnInfo(name = "overall_rating")
    val overallRating: Float,
    val remarks: String
)
