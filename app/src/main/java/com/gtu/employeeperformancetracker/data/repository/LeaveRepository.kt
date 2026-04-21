package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LeaveRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val requests = store.firestore().collection(LEAVE_COLLECTION)

    fun getAllLeaveRequests(): Flow<List<LeaveRequest>> = store.observeCollection(requests) { it.toLeaveRequest() }

    fun getLeaveRequestsByEmployee(employeeId: Int): Flow<List<LeaveRequest>> = getAllLeaveRequests()
        .map { items -> items.filter { it.employeeId == employeeId } }

    suspend fun insert(request: LeaveRequest) {
        val requestId = store.nextId(LEAVE_COUNTER_KEY)
        requests.document(requestId.toString())
            .set(request.copy(id = requestId).toMap())
            .await()
    }

    suspend fun update(request: LeaveRequest) {
        requests.document(request.id.toString()).set(request.toMap()).await()
    }

    private fun LeaveRequest.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employeeId" to employeeId,
        "leaveType" to leaveType,
        "startDate" to startDate,
        "endDate" to endDate,
        "reason" to reason,
        "status" to status,
        "appliedAt" to appliedAt
    )

    private fun DocumentSnapshot.toLeaveRequest(): LeaveRequest? {
        val employeeId = getLong("employeeId")?.toInt() ?: return null
        return LeaveRequest(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            employeeId = employeeId,
            leaveType = getString("leaveType").orEmpty(),
            startDate = getString("startDate").orEmpty(),
            endDate = getString("endDate").orEmpty(),
            reason = getString("reason").orEmpty(),
            status = getString("status").orEmpty(),
            appliedAt = getString("appliedAt").orEmpty()
        )
    }

    companion object {
        private const val LEAVE_COLLECTION = "leave_requests"
        private const val LEAVE_COUNTER_KEY = "leave_requests"
    }
}
