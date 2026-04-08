package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.FinanceMovement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("finance_movements")

    fun getAllFlow(profile: String): Flow<List<FinanceMovement>> = callbackFlow {
        val listener = collection
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FinanceRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val movements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FinanceMovement::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(movements)
            }
        awaitClose { listener.remove() }
    }

    fun getByDate(date: String, profile: String): Flow<List<FinanceMovement>> = callbackFlow {
        val listener = collection
            .whereEqualTo("date", date)
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FinanceRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val movements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FinanceMovement::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(movements)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(movement: FinanceMovement) {
        try {
            collection.add(movement).await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error insertando: ${e.message}")
        }
    }

    suspend fun delete(movementId: String) {
        try {
            collection.document(movementId).delete().await()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error eliminando: ${e.message}")
        }
    }
}
