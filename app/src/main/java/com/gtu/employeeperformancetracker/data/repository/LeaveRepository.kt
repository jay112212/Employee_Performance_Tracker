package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.LeaveRequestDao
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import kotlinx.coroutines.flow.Flow

class LeaveRepository(private val dao: LeaveRequestDao) {

    suspend fun insert(request: LeaveRequest) = dao.insertLeaveRequest(request)

    suspend fun update(request: LeaveRequest) = dao.updateLeaveRequest(request)

    fun getAllLeaveRequests(): Flow<List<LeaveRequest>> = dao.getAllLeaveRequests()

    fun getLeaveRequestsByEmployee(employeeId: Int): Flow<List<LeaveRequest>> =
        dao.getLeaveRequestsByEmployee(employeeId)
}
