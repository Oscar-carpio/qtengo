package com.example.qtengo.core.data.repositories

import com.example.qtengo.core.domain.models.StockMovement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StockMovementRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("stock_movements")

    fun getMovementsByDate(date: String, profile: String): Flow<List<StockMovement>> = callbackFlow {
        val listener = collection
            .whereEqualTo("date", date)
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val movements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StockMovement::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(movements)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(movement: StockMovement) {
        collection.add(movement).await()
    }
}
