package com.gtu.employeeperformancetracker.data.remote

import android.content.Context
import com.gtu.employeeperformancetracker.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first

object FirebaseSyncCoordinator {

    suspend fun syncAll(context: Context) {
        val firebaseSyncService = FirebaseSyncService.getInstance(context)
        if (!firebaseSyncService.isConfigured) return

        val database = AppDatabase.getDatabase(context)

        database.employeeDao().getAllEmployees().first().forEach { employee ->
            firebaseSyncService.syncEmployee(employee)
        }

        database.taskDao().getAllTasks().first().forEach { task ->
            firebaseSyncService.syncTask(task)
        }

        database.performanceDao().getAllReviews().first().forEach { review ->
            firebaseSyncService.syncPerformance(review)
        }

        database.authUserDao().observeUsers().first().forEach { user ->
            firebaseSyncService.syncAuthUser(user)
        }

        database.emailLogDao().observeEmails().first().forEach { email ->
            firebaseSyncService.syncEmailLog(email)
        }

        database.attendanceDao().getAllAttendance().first().forEach { record ->
            firebaseSyncService.syncAttendance(record)
        }

        database.leaveRequestDao().getAllLeaveRequests().first().forEach { request ->
            firebaseSyncService.syncLeaveRequest(request)
        }
    }
}
