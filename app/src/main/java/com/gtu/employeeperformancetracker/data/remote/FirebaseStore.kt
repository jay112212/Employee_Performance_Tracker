package com.gtu.employeeperformancetracker.data.remote

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseStore(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun <T> observeCollection(
        query: Query,
        mapper: (DocumentSnapshot) -> T?
    ): Flow<List<T>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents
                ?.mapNotNull(mapper)
                .orEmpty()

            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    fun <T> observeDocument(
        document: DocumentReference,
        mapper: (DocumentSnapshot) -> T?
    ): Flow<T?> = callbackFlow {
        val registration = document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            trySend(snapshot?.let(mapper))
        }

        awaitClose { registration.remove() }
    }

    suspend fun nextId(counterKey: String): Int {
        val counterRef = firestore.collection(META_COLLECTION).document(COUNTERS_DOCUMENT)
        val nextValue = firestore.runTransaction { transaction ->
            val current = transaction.get(counterRef).getLong(counterKey) ?: 0L
            val next = current + 1L
            transaction.set(counterRef, mapOf(counterKey to next), SetOptions.merge())
            next
        }.await()

        return nextValue.toInt()
    }

    fun firestore(): FirebaseFirestore = firestore

    companion object {
        private const val META_COLLECTION = "app_meta"
        private const val COUNTERS_DOCUMENT = "counters"
    }
}
