package com.gtu.employeeperformancetracker.data.remote

import android.content.Context
import android.util.Log
import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import com.gtu.employeeperformancetracker.data.local.entity.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseSyncService private constructor(context: Context) {

    private val firestore: FirebaseFirestore? = runCatching {
        val firebaseApp = FirebaseApp.getApps(context).firstOrNull()
            ?: FirebaseApp.initializeApp(context)
        firebaseApp?.let { FirebaseFirestore.getInstance(it) }
    }.getOrNull()

    val isConfigured: Boolean
        get() = firestore != null

    suspend fun syncEmployee(employee: Employee) {
        setDocument(
            collection = EMPLOYEES,
            documentId = employee.id.toString(),
            payload = mapOf(
                "id" to employee.id,
                "employeeCode" to employee.employeeCode,
                "name" to employee.name,
                "role" to employee.role,
                "department" to employee.department,
                "joiningDate" to employee.joiningDate,
                "email" to employee.email,
                "contact" to employee.contact,
                "profilePictureUri" to employee.profilePictureUri
            )
        )
    }

    suspend fun deleteEmployee(employeeId: Int) {
        deleteDocument(EMPLOYEES, employeeId.toString())
    }

    suspend fun syncTask(task: Task) {
        setDocument(
            collection = TASKS,
            documentId = task.id.toString(),
            payload = mapOf(
                "id" to task.id,
                "employeeId" to task.employeeId,
                "description" to task.description,
                "deadline" to task.deadline,
                "priority" to task.priority,
                "assignedDate" to task.assignedDate,
                "status" to task.status
            )
        )
    }

    suspend fun deleteTask(taskId: Int) {
        deleteDocument(TASKS, taskId.toString())
    }

    suspend fun syncPerformance(review: Performance) {
        setDocument(
            collection = PERFORMANCE_REVIEWS,
            documentId = review.id.toString(),
            payload = mapOf(
                "id" to review.id,
                "employeeId" to review.employeeId,
                "reviewDate" to review.reviewDate,
                "qualityScore" to review.qualityScore,
                "timelinessScore" to review.timelinessScore,
                "attendanceScore" to review.attendanceScore,
                "communicationScore" to review.communicationScore,
                "innovationScore" to review.innovationScore,
                "overallRating" to review.overallRating,
                "remarks" to review.remarks
            )
        )
    }

    suspend fun deletePerformance(reviewId: Int) {
        deleteDocument(PERFORMANCE_REVIEWS, reviewId.toString())
    }

    suspend fun syncAttendance(record: AttendanceRecord) {
        setDocument(
            collection = ATTENDANCE,
            documentId = record.id.toString(),
            payload = mapOf(
                "id" to record.id,
                "employeeId" to record.employeeId,
                "date" to record.date,
                "status" to record.status,
                "checkInTime" to record.checkInTime,
                "checkOutTime" to record.checkOutTime,
                "location" to record.location
            )
        )
    }

    suspend fun syncLeaveRequest(request: LeaveRequest) {
        setDocument(
            collection = LEAVE_REQUESTS,
            documentId = request.id.toString(),
            payload = mapOf(
                "id" to request.id,
                "employeeId" to request.employeeId,
                "leaveType" to request.leaveType,
                "startDate" to request.startDate,
                "endDate" to request.endDate,
                "reason" to request.reason,
                "status" to request.status,
                "appliedAt" to request.appliedAt
            )
        )
    }

    suspend fun syncEmailLog(emailLog: EmailLog) {
        setDocument(
            collection = EMAIL_LOGS,
            documentId = emailLog.id.toString(),
            payload = mapOf(
                "id" to emailLog.id,
                "recipientEmail" to emailLog.recipientEmail,
                "recipientName" to emailLog.recipientName,
                "subject" to emailLog.subject,
                "body" to emailLog.body,
                "sentAt" to emailLog.sentAt
            )
        )
    }

    suspend fun syncAuthUser(user: AuthUser) {
        setDocument(
            collection = AUTH_USERS,
            documentId = user.id.toString(),
            payload = mapOf(
                "id" to user.id,
                "employeeId" to user.employeeId,
                "displayName" to user.displayName,
                "email" to user.email,
                "role" to user.role,
                "createdAt" to user.createdAt,
                "forcePasswordReset" to user.forcePasswordReset,
                "isActive" to user.isActive
            )
        )
    }

    suspend fun deleteAuthUser(userId: Int) {
        deleteDocument(AUTH_USERS, userId.toString())
    }

    private suspend fun setDocument(
        collection: String,
        documentId: String,
        payload: Map<String, Any?>
    ) {
        val instance = firestore ?: return
        runCatching {
            instance.collection(collection)
                .document(documentId)
                .set(payload)
                .await()
        }.onFailure { error ->
            Log.w(TAG, "Unable to sync $collection/$documentId to Firebase.", error)
        }
    }

    private suspend fun deleteDocument(collection: String, documentId: String) {
        val instance = firestore ?: return
        runCatching {
            instance.collection(collection)
                .document(documentId)
                .delete()
                .await()
        }.onFailure { error ->
            Log.w(TAG, "Unable to delete $collection/$documentId from Firebase.", error)
        }
    }

    companion object {
        private const val TAG = "FirebaseSyncService"
        private const val EMPLOYEES = "employees"
        private const val TASKS = "tasks"
        private const val PERFORMANCE_REVIEWS = "performance_reviews"
        private const val AUTH_USERS = "auth_users"
        private const val EMAIL_LOGS = "email_logs"
        private const val ATTENDANCE = "attendance_records"
        private const val LEAVE_REQUESTS = "leave_requests"

        @Volatile
        private var INSTANCE: FirebaseSyncService? = null

        fun getInstance(context: Context): FirebaseSyncService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseSyncService(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
