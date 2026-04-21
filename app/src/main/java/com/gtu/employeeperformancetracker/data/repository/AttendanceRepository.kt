package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AttendanceRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val records = store.firestore().collection(ATTENDANCE_COLLECTION)

    fun getAllAttendance(): Flow<List<AttendanceRecord>> = store.observeCollection(records) { it.toAttendance() }

    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecord>> = getAllAttendance()
        .map { items -> items.filter { it.employeeId == employeeId } }

    suspend fun getAttendanceByEmployeeAndDate(employeeId: Int, date: String): AttendanceRecord? =
        getAllAttendance().first().find { it.employeeId == employeeId && it.date == date }

    suspend fun insert(record: AttendanceRecord) {
        val recordId = store.nextId(ATTENDANCE_COUNTER_KEY)
        records.document(recordId.toString())
            .set(record.copy(id = recordId).toMap())
            .await()
    }

    suspend fun update(record: AttendanceRecord) {
        records.document(record.id.toString()).set(record.toMap()).await()
    }

    private fun AttendanceRecord.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employeeId" to employeeId,
        "date" to date,
        "status" to status,
        "checkInTime" to checkInTime,
        "checkOutTime" to checkOutTime,
        "location" to location
    )

    private fun DocumentSnapshot.toAttendance(): AttendanceRecord? {
        val employeeId = getLong("employeeId")?.toInt() ?: return null
        return AttendanceRecord(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            employeeId = employeeId,
            date = getString("date").orEmpty(),
            status = getString("status").orEmpty(),
            checkInTime = getString("checkInTime"),
            checkOutTime = getString("checkOutTime"),
            location = getString("location")
        )
    }

    companion object {
        private const val ATTENDANCE_COLLECTION = "attendance_records"
        private const val ATTENDANCE_COUNTER_KEY = "attendance_records"
    }
}
