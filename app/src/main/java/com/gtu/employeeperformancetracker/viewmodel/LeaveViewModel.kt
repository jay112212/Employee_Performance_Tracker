package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.database.AppDatabase
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import com.gtu.employeeperformancetracker.data.repository.LeaveRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class LeaveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeaveRepository(
        AppDatabase.getDatabase(application).leaveRequestDao()
    )

    val leaveRequests: StateFlow<List<LeaveRequest>> = repository.getAllLeaveRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun applyLeave(
        employeeId: Int,
        leaveType: String,
        startDate: String,
        endDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.insert(
                LeaveRequest(
                    employeeId = employeeId,
                    leaveType = leaveType,
                    startDate = startDate,
                    endDate = endDate,
                    reason = reason,
                    appliedAt = LocalDateTime.now().toString()
                )
            )
        }
    }

    fun updateStatus(request: LeaveRequest, status: String) {
        viewModelScope.launch {
            repository.update(request.copy(status = status))
        }
    }
}
