package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("products")

    /**
     * Obtiene los productos filtrados por perfil en tiempo real.
     * Captura errores de permisos para evitar el cierre de la app.
     */
    fun getByProfileFlow(profile: String): Flow<List<Product>> = callbackFlow {
        val listener = collection
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepository", "Error en Firestore: ${error.message}")
                    // En lugar de cerrar el flow con error, enviamos lista vacía
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
        try {
            collection.add(product).await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error insertando: ${e.message}")
        }
    }

    suspend fun update(product: Product, documentId: String) {
        try {
            collection.document(documentId).set(product).await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error actualizando: ${e.message}")
        }
    }

    suspend fun delete(documentId: String) {
        try {
            collection.document(documentId).delete().await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error eliminando: ${e.message}")
        }
    }
}
