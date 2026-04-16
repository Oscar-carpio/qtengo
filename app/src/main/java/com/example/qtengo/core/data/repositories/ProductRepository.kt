package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun collection() = firestore
        .collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("products")

    fun getByProfileFlow(profile: String): Flow<List<Product>> = callbackFlow {
        val listener = collection()
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepository", "Error en Firestore: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(product: Product) {
        try { collection().add(product).await() }
        catch (e: Exception) { Log.e("ProductRepository", "Error insertando: ${e.message}") }
    }

    suspend fun update(product: Product, documentId: String) {
        try { collection().document(documentId).set(product).await() }
        catch (e: Exception) { Log.e("ProductRepository", "Error actualizando: ${e.message}") }
    }

    suspend fun delete(documentId: String) {
        try { collection().document(documentId).delete().await() }
        catch (e: Exception) { Log.e("ProductRepository", "Error eliminando: ${e.message}") }
    }
}