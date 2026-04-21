package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employee_id = :employeeId ORDER BY date DESC, id DESC")
    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employee_id = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceByEmployeeAndDate(employeeId: Int, date: String): AttendanceRecord?
}
