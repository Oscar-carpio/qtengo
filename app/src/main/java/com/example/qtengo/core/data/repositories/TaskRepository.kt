package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("tasks")

    fun getByProfileFlow(profile: String): Flow<List<Task>> = callbackFlow {
        val listener = collection
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TaskRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    fun getByDate(date: String, profile: String): Flow<List<Task>> = callbackFlow {
        val listener = collection
            .whereEqualTo("date", date)
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TaskRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(task: Task) {
        try {
            collection.add(task).await()
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error insertando: ${e.message}")
        }
    }

    suspend fun update(task: Task) {
        try {
            collection.document(task.id).set(task).await()
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error actualizando: ${e.message}")
        }
    }

    suspend fun delete(taskId: String) {
        try {
            collection.document(taskId).delete().await()
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error eliminando: ${e.message}")
        }
    }
}
