package com.gtu.employeeperformancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Performance): Long

    @Update
    suspend fun updateReview(review: Performance)

    @Delete
    suspend fun deleteReview(review: Performance)

    @Query("SELECT * FROM performance_reviews ORDER BY review_date DESC")
    fun getAllReviews(): Flow<List<Performance>>

    @Query("SELECT * FROM performance_reviews WHERE employee_id = :employeeId ORDER BY review_date DESC")
    fun getReviewsByEmployee(employeeId: Int): Flow<List<Performance>>
}
