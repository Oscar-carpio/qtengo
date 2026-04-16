package com.example.qtengo.core.data.repositories

import com.example.qtengo.core.domain.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun collection() = firestore
        .collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("expenses")

    fun getByProfileFlow(profile: String): Flow<List<Expense>> = callbackFlow {
        val listener = collection()
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(expense: Expense) { collection().add(expense).await() }
    suspend fun update(expense: Expense) { collection().document(expense.id).set(expense).await() }
    suspend fun delete(expense: Expense) { collection().document(expense.id).delete().await() }
}