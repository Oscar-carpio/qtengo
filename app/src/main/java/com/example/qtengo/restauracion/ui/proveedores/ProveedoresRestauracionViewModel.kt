package com.example.qtengo.restauracion.ui.proveedores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Proveedor(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",   // antes "productos" en v1 — unificado como "direccion"
    val notas: String = ""
)

class ProveedoresRestauracionViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _proveedores = MutableStateFlow<List<Proveedor>>(emptyList())
    val proveedores: StateFlow<List<Proveedor>> = _proveedores

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var proveedoresListener: ListenerRegistration? = null

    // ─── Filtro combinado ────────────────────────────────────────────────────

    val proveedoresFiltrados: StateFlow<List<Proveedor>> =
        combine(_proveedores, _filtro) { lista, texto ->
            val filtroLimpio = texto.trim()
            if (filtroLimpio.isBlank()) {
                lista
            } else {
                lista.filter { proveedor ->
                    proveedor.nombre.contains(filtroLimpio, ignoreCase = true) ||
                            proveedor.telefono.contains(filtroLimpio, ignoreCase = true) ||
                            proveedor.email.contains(filtroLimpio, ignoreCase = true) ||
                            proveedor.direccion.contains(filtroLimpio, ignoreCase = true) ||
                            proveedor.notas.contains(filtroLimpio, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun actualizarFiltro(valor: String) {
        _filtro.value = valor
    }

    fun clearError() {
        _error.value = null
    }

    // ─── Helpers internos ────────────────────────────────────────────────────

    private fun requireUid(): String? {
        if (uid.isBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return uid
    }

    private fun proveedoresRef() =
        db.collection("usuarios").document(uid).collection("proveedoresRestauracion")

    // ─── Carga de datos ──────────────────────────────────────────────────────

    fun cargarProveedores() {
        val uid = requireUid() ?: return
        proveedoresListener?.remove()
        proveedoresListener = db.collection("usuarios").document(uid)
            .collection("proveedoresRestauracion")
            .orderBy("nombre")                      // ordenación en servidor
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar proveedores: ${e.message}"
                    return@addSnapshotListener
                }
                _proveedores.value = snapshot?.documents?.map { doc ->
                    Proveedor(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        email = doc.getString("email") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        notas = doc.getString("notas") ?: ""
                    )
                } ?: emptyList()
            }
    }

    // ─── Escritura ───────────────────────────────────────────────────────────

    fun agregarProveedor(proveedor: Proveedor) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "nombre" to proveedor.nombre.trim(),
                    "telefono" to proveedor.telefono.trim(),
                    "email" to proveedor.email.trim(),
                    "direccion" to proveedor.direccion.trim(),
                    "notas" to proveedor.notas.trim()
                )
                proveedoresRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al agregar proveedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editarProveedor(id: String, proveedor: Proveedor) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "nombre" to proveedor.nombre.trim(),
                    "telefono" to proveedor.telefono.trim(),
                    "email" to proveedor.email.trim(),
                    "direccion" to proveedor.direccion.trim(),
                    "notas" to proveedor.notas.trim()
                )
                proveedoresRef().document(id).update(data).await()
            } catch (e: Exception) {
                _error.value = "Error al editar proveedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarProveedor(id: String) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                proveedoresRef().document(id).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar proveedor: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─── Cleanup ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        proveedoresListener?.remove()
    }
}