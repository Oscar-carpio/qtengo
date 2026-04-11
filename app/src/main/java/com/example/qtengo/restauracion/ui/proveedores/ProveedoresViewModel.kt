package com.example.qtengo.restauracion.ui.proveedores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Proveedor(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val email: String = "",
    val productos: String = "",
    val notas: String = ""
)

class ProveedoresViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _proveedores = MutableStateFlow<List<Proveedor>>(emptyList())
    val proveedores: StateFlow<List<Proveedor>> = _proveedores

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var proveedoresListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private fun requireUid(): String? {
        if (uid.isBlank()) { _error.value = "Usuario no autenticado"; return null }
        return uid
    }

    private fun proveedoresRef() = db.collection("usuarios").document(uid).collection("restauracion_proveedores")

    fun cargarProveedores() {
        val uid = requireUid() ?: return
        proveedoresListener?.remove()
        proveedoresListener = db.collection("usuarios").document(uid).collection("restauracion_proveedores")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _error.value = "Error al cargar proveedores: ${e.message}"; return@addSnapshotListener }
                _proveedores.value = snapshot?.documents?.map { doc ->
                    Proveedor(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        email = doc.getString("email") ?: "",
                        productos = doc.getString("productos") ?: "",
                        notas = doc.getString("notas") ?: ""
                    )
                }?.sortedBy { it.nombre } ?: emptyList()
            }
    }

    fun añadirProveedor(proveedor: Proveedor) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to proveedor.nombre,
                    "telefono" to proveedor.telefono,
                    "email" to proveedor.email,
                    "productos" to proveedor.productos,
                    "notas" to proveedor.notas
                )
                proveedoresRef().add(data).await()
            } catch (e: Exception) { _error.value = "Error al añadir proveedor: ${e.message}" }
        }
    }

    fun editarProveedor(proveedorId: String, proveedor: Proveedor) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                proveedoresRef().document(proveedorId).update(
                    "nombre", proveedor.nombre,
                    "telefono", proveedor.telefono,
                    "email", proveedor.email,
                    "productos", proveedor.productos,
                    "notas", proveedor.notas
                ).await()
            } catch (e: Exception) { _error.value = "Error al editar proveedor: ${e.message}" }
        }
    }

    fun eliminarProveedor(proveedorId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            try { proveedoresRef().document(proveedorId).delete().await() }
            catch (e: Exception) { _error.value = "Error al eliminar proveedor: ${e.message}" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        proveedoresListener?.remove()
    }
}
