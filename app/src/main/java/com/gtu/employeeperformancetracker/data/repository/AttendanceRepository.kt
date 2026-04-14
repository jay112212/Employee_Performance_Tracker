package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.AttendanceDao
import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val dao: AttendanceDao) {

    suspend fun insert(record: AttendanceRecord) = dao.insertAttendance(record)

    suspend fun update(record: AttendanceRecord) = dao.updateAttendance(record)

    fun getAllAttendance(): Flow<List<AttendanceRecord>> = dao.getAllAttendance()

    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecord>> =
        dao.getAttendanceByEmployee(employeeId)

    suspend fun getAttendanceByEmployeeAndDate(employeeId: Int, date: String): AttendanceRecord? =
        dao.getAttendanceByEmployeeAndDate(employeeId, date)
}
