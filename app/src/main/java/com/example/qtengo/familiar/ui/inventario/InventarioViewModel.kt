package com.example.qtengo.familiar.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
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

    // FIX CRIT #2 — Canal de errores para la UI
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // FIX CRIT #1 — Guardamos el listener para cancelarlo en onCleared()
    private var itemsListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    // FIX CRIT #3 — Guard centralizado contra uid vacío
    private fun requireUid(): String? {
        if (uid.isBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return uid
    }

    private fun inventarioRef() = db.collection("usuarios").document(uid).collection("inventario")

    // ─── Carga de datos ──────────────────────────────────────────────────────

    fun cargarItems() {
        val uid = requireUid() ?: return

        // FIX CRIT #1 — cancelamos listener anterior antes de suscribirse
        itemsListener?.remove()
        itemsListener = db.collection("usuarios").document(uid).collection("inventario")
            .addSnapshotListener { snapshot, e ->
                // FIX CRIT #2 — capturamos error del listener
                if (e != null) {
                    _error.value = "Error al cargar inventario: ${e.message}"
                    return@addSnapshotListener
                }
                _items.value = snapshot?.documents?.map { doc ->
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
            }
    }

    // ─── Escritura ───────────────────────────────────────────────────────────

    fun añadirItem(
        nombre: String,
        cantidad: Int,
        ubicacion: String,
        minStock: Int,
        notas: String,
        fechaCaducidad: String?
    ) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mutableMapOf<String, Any>(
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
            } catch (e: Exception) {
                _error.value = "Error al añadir artículo: ${e.message}"
            }
        }
    }

    /** Edita un artículo existente del inventario */
    fun editarItem(
        itemId: String,
        nombre: String,
        cantidad: Int,
        ubicacion: String,
        minStock: Int,
        notas: String,
        fechaCaducidad: String?
    ) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = hashMapOf<String, Any>(
                    "nombre" to nombre,
                    "cantidad" to cantidad,
                    "ubicacion" to ubicacion,
                    "minStock" to minStock,
                    "notas" to notas,
                    "fechaCaducidad" to (fechaCaducidad ?: FieldValue.delete())
                )
                inventarioRef().document(itemId).update(data).await()
            } catch (e: Exception) {
                _error.value = "Error al editar artículo: ${e.message}"
            }
        }
    }

    fun eliminarItem(itemId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                inventarioRef().document(itemId).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar artículo: ${e.message}"
            }
        }
    }

    fun actualizarCantidad(itemId: String, nuevaCantidad: Int) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                inventarioRef().document(itemId).update("cantidad", nuevaCantidad).await()
            } catch (e: Exception) {
                _error.value = "Error al actualizar cantidad: ${e.message}"
            }
        }
    }

    // FIX CRIT #1 — cancelamos el listener al destruirse el ViewModel
    override fun onCleared() {
        super.onCleared()
        itemsListener?.remove()
    }
}