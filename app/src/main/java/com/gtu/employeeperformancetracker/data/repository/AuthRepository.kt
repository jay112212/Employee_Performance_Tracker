package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.AuthUserDao
import com.gtu.employeeperformancetracker.data.local.dao.SessionDao
import com.gtu.employeeperformancetracker.data.local.entity.AppSession
import com.gtu.employeeperformancetracker.data.local.entity.AuthUser
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val userDao: AuthUserDao,
    private val sessionDao: SessionDao
) {

    fun observeUsers(): Flow<List<AuthUser>> = userDao.observeUsers()

    fun observeSession(): Flow<AppSession?> = sessionDao.observeSession()

    fun observeUserById(id: Int): Flow<AuthUser?> = userDao.observeUserById(id)

    suspend fun insertUser(user: AuthUser): Long = userDao.insertUser(user)

    suspend fun updateUser(user: AuthUser) = userDao.updateUser(user)

    suspend fun getUserByEmail(email: String): AuthUser? = userDao.getUserByEmail(email)

    suspend fun authenticate(email: String, password: String): AuthUser? =
        userDao.authenticate(email, password)

    suspend fun saveSession(userId: Int?) {
        sessionDao.saveSession(AppSession(currentUserId = userId))
    }

    suspend fun deleteEmployeeAccount(employeeId: Int) {
        userDao.deleteByEmployeeId(employeeId)
    }
}
