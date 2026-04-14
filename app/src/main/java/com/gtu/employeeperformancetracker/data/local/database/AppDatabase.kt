package com.gtu.employeeperformancetracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gtu.employeeperformancetracker.data.local.dao.EmployeeDao
import com.gtu.employeeperformancetracker.data.local.dao.AuthUserDao
import com.gtu.employeeperformancetracker.data.local.dao.AttendanceDao
import com.gtu.employeeperformancetracker.data.local.dao.EmailLogDao
import com.gtu.employeeperformancetracker.data.local.dao.PerformanceDao
import com.gtu.employeeperformancetracker.data.local.dao.SessionDao
import com.gtu.employeeperformancetracker.data.local.dao.TaskDao
import com.gtu.employeeperformancetracker.data.local.dao.LeaveRequestDao
import com.gtu.employeeperformancetracker.data.local.entity.AppSession
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import com.gtu.employeeperformancetracker.data.local.entity.AttendanceRecord
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.local.entity.EmailLog
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import com.gtu.employeeperformancetracker.data.local.entity.Task

@Database(
    entities = [
        Employee::class,
        Task::class,
        Performance::class,
        AuthUser::class,
        AppSession::class,
        EmailLog::class,
        AttendanceRecord::class,
        LeaveRequest::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao
    abstract fun taskDao(): TaskDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun authUserDao(): AuthUserDao
    abstract fun sessionDao(): SessionDao
    abstract fun emailLogDao(): EmailLogDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveRequestDao(): LeaveRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "employee_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
