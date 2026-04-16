package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.Supplier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SupplierRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // FIX — ruta bajo usuarios/{uid}/suppliers para cumplir las reglas de Firestore
    private fun collection() = firestore
        .collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("suppliers")

    fun getByProfileFlow(profile: String): Flow<List<Supplier>> = callbackFlow {
        val listener = collection()
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SupplierRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val suppliers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Supplier::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(suppliers)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(supplier: Supplier) {
        try {
            collection().add(supplier).await()
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Error insertando: ${e.message}")
        }
    }

    suspend fun update(supplier: Supplier) {
        try {
            collection().document(supplier.id).set(supplier).await()
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Error actualizando: ${e.message}")
        }
    }

    suspend fun delete(supplierId: String) {
        try {
            collection().document(supplierId).delete().await()
        } catch (e: Exception) {
            Log.e("SupplierRepository", "Error eliminando: ${e.message}")
        }
    }
}