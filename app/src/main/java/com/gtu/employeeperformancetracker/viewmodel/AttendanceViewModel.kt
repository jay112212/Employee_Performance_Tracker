package com.gtu.employeeperformancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import com.gtu.employeeperformancetracker.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository()

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.getAllAttendance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun checkIn(
        employeeId: Int,
        location: String,
        status: String = "Present"
    ) {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val existing = repository.getAttendanceByEmployeeAndDate(employeeId, date)
            val now = LocalTime.now().withSecond(0).withNano(0).toString()

            if (existing == null) {
                repository.insert(
                    AttendanceRecord(
                        employeeId = employeeId,
                        date = date,
                        status = status,
                        checkInTime = now,
                        location = location.ifBlank { null }
                    )
                )
            } else {
                repository.update(
                    existing.copy(
                        status = status,
                        checkInTime = existing.checkInTime ?: now,
                        location = location.ifBlank { existing.location }
                    )
                )
            }
        }
    }

    fun checkOut(employeeId: Int) {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val existing = repository.getAttendanceByEmployeeAndDate(employeeId, date) ?: return@launch
            val now = LocalTime.now().withSecond(0).withNano(0).toString()
            repository.update(existing.copy(checkOutTime = now))
        }
    }
}
