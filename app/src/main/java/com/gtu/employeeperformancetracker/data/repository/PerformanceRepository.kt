package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.PerformanceDao
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import kotlinx.coroutines.flow.Flow

class PerformanceRepository(private val dao: PerformanceDao) {

    suspend fun insert(review: Performance) = dao.insertReview(review)

    suspend fun update(review: Performance) = dao.updateReview(review)

    suspend fun delete(review: Performance) = dao.deleteReview(review)

    fun getAllReviews(): Flow<List<Performance>> = dao.getAllReviews()

    fun getReviewsByEmployee(employeeId: Int): Flow<List<Performance>> = dao.getReviewsByEmployee(employeeId)
}
