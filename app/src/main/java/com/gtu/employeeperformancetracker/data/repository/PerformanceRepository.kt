package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.Performance
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PerformanceRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val reviews = store.firestore().collection(REVIEWS_COLLECTION)

    fun getAllReviews(): Flow<List<Performance>> = store.observeCollection(reviews) { it.toPerformance() }

    fun getReviewsByEmployee(employeeId: Int): Flow<List<Performance>> = getAllReviews()
        .map { items -> items.filter { it.employeeId == employeeId } }

    suspend fun insert(review: Performance) {
        val reviewId = store.nextId(REVIEW_COUNTER_KEY)
        reviews.document(reviewId.toString())
            .set(review.copy(id = reviewId).toMap())
            .await()
    }

    suspend fun update(review: Performance) {
        reviews.document(review.id.toString()).set(review.toMap()).await()
    }

    suspend fun delete(review: Performance) {
        reviews.document(review.id.toString()).delete().await()
    }

    private fun Performance.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employeeId" to employeeId,
        "reviewDate" to reviewDate,
        "qualityScore" to qualityScore,
        "timelinessScore" to timelinessScore,
        "attendanceScore" to attendanceScore,
        "communicationScore" to communicationScore,
        "innovationScore" to innovationScore,
        "overallRating" to overallRating,
        "remarks" to remarks
    )

    private fun DocumentSnapshot.toPerformance(): Performance? {
        val employeeId = getLong("employeeId")?.toInt() ?: return null
        return Performance(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            employeeId = employeeId,
            reviewDate = getString("reviewDate").orEmpty(),
            qualityScore = getLong("qualityScore")?.toInt() ?: 0,
            timelinessScore = getLong("timelinessScore")?.toInt() ?: 0,
            attendanceScore = getLong("attendanceScore")?.toInt() ?: 0,
            communicationScore = getLong("communicationScore")?.toInt() ?: 0,
            innovationScore = getLong("innovationScore")?.toInt() ?: 0,
            overallRating = (getDouble("overallRating") ?: 0.0).toFloat(),
            remarks = getString("remarks").orEmpty()
        )
    }

    companion object {
        private const val REVIEWS_COLLECTION = "performance_reviews"
        private const val REVIEW_COUNTER_KEY = "performance_reviews"
    }
}
