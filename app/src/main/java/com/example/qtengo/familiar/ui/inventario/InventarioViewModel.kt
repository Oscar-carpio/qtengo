package com.example.qtengo.familiar.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class InventarioItem(
    val id: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val ubicacion: String = "",
    val minStock: Int = 1,
    val notas: String = "",
    val fechaCaducidad: String? = null
)

class InventarioViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _items = MutableStateFlow<List<InventarioItem>>(emptyList())
    val items: StateFlow<List<InventarioItem>> = _items

    /** Referencia base del inventario en Firestore */
    private fun inventarioRef() = db.collection("usuarios").document(uid).collection("inventario")

    /** Carga todos los artículos del usuario en tiempo real */
    fun cargarItems() {
        inventarioRef().addSnapshotListener { snapshot, _ ->
            val result = snapshot?.documents?.map { doc ->
                InventarioItem(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    cantidad = (doc.getLong("cantidad") ?: 0).toInt(),
                    ubicacion = doc.getString("ubicacion") ?: "",
                    minStock = (doc.getLong("minStock") ?: 1).toInt(),
                    notas = doc.getString("notas") ?: "",
                    fechaCaducidad = doc.getString("fechaCaducidad")
                )
            } ?: emptyList()
            _items.value = result
        }
    }

    /** Añade un nuevo artículo al inventario */
    fun añadirItem(
        nombre: String,
        cantidad: Int,
        ubicacion: String,
        minStock: Int,
        notas: String,
        fechaCaducidad: String?
    ) {
        viewModelScope.launch {
            val data = mutableMapOf(
                "nombre" to nombre,
                "cantidad" to cantidad,
                "ubicacion" to ubicacion,
                "minStock" to minStock,
                "notas" to notas
            )
            if (!fechaCaducidad.isNullOrBlank()) {
                data["fechaCaducidad"] = fechaCaducidad
            }
            inventarioRef().add(data).await()
        }
    }

    /** Elimina un artículo del inventario */
    fun eliminarItem(itemId: String) {
        viewModelScope.launch {
            inventarioRef().document(itemId).delete().await()
        }
    }

    /** Actualiza la cantidad de un artículo */
    fun actualizarCantidad(itemId: String, nuevaCantidad: Int) {
        viewModelScope.launch {
            inventarioRef().document(itemId).update("cantidad", nuevaCantidad).await()
        }
    }
}