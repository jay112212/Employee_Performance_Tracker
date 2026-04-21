package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import com.gtu.employeeperformancetracker.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PerformanceRepository()

    val reviews: StateFlow<List<Performance>> = repository.getAllReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addReview(
        employeeId: Int,
        reviewDate: String,
        qualityScore: Int,
        timelinessScore: Int,
        attendanceScore: Int,
        communicationScore: Int,
        innovationScore: Int,
        overallRating: Float,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.insert(
                Performance(
                    employeeId = employeeId,
                    reviewDate = reviewDate,
                    qualityScore = qualityScore,
                    timelinessScore = timelinessScore,
                    attendanceScore = attendanceScore,
                    communicationScore = communicationScore,
                    innovationScore = innovationScore,
                    overallRating = overallRating,
                    remarks = remarks
                )
            )
        }
    }

    fun deleteReview(review: Performance) {
        viewModelScope.launch {
            repository.delete(review)
        }
    }
}
