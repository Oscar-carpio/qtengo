package com.example.qtengo.restauracion.ui.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class RestauracionReserva(
    val id: String = "",
    val nombreCliente: String = "",
    val fecha: Long = 0L,
    val comensales: Int = 0,
    val notas: String = "",
    val estado: String = "Pendiente",   // Confirmada | Pendiente | Cancelada
    val email: String = "",
    val telefono: String = ""
)

class ReservasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _reservas = MutableStateFlow<List<RestauracionReserva>>(emptyList())
    val reservas: StateFlow<List<RestauracionReserva>> = _reservas.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var reservasListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private fun requireUid(): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return currentUid
    }

    private fun reservasRef(uid: String) = db.collection("usuarios").document(uid).collection("reservas")

    val reservasFiltradas: StateFlow<List<RestauracionReserva>> =
        combine(_reservas, _filtro) { lista, texto ->
            val filtroLimpio = texto.trim()
            if (filtroLimpio.isBlank()) {
                lista
            } else {
                lista.filter { reserva ->
                    reserva.nombreCliente.contains(filtroLimpio, ignoreCase = true) ||
                            reserva.notas.contains(filtroLimpio, ignoreCase = true) ||
                            reserva.comensales.toString().contains(filtroLimpio) ||
                            reserva.estado.contains(filtroLimpio, ignoreCase = true)
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

    fun cargarReservas() {
        val uid = requireUid() ?: return
        reservasListener?.remove()
        reservasListener = db.collection("usuarios").document(uid).collection("reservas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar reservas: ${e.message}"
                    return@addSnapshotListener
                }
                _reservas.value = snapshot?.documents?.map { doc ->
                    RestauracionReserva(
                        id = doc.id,
                        nombreCliente = doc.getString("nombreCliente") ?: "",
                        fecha = doc.getLong("fecha") ?: 0L,
                        comensales = (doc.getLong("comensales") ?: 0L).toInt(),
                        notas = doc.getString("notas") ?: "",
                        estado = doc.getString("estado") ?: "Pendiente",
                        email = doc.getString("email") ?: "",
                        telefono = doc.getString("telefono") ?: ""
                    )
                }?.sortedBy { it.fecha } ?: emptyList()
            }
    }

    fun agregarReserva(
        nombre: String,
        comensales: Int,
        notas: String,
        fecha: Long = System.currentTimeMillis(),
        email: String = "",
        telefono: String = ""
    ) {
        val uid = requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "nombreCliente" to nombre.trim(),
                    "comensales" to comensales,
                    "notas" to notas.trim(),
                    "fecha" to fecha,
                    "estado" to "Pendiente",
                    "email" to email.trim(),
                    "telefono" to telefono.trim()
                )
                reservasRef(uid).add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al agregar reserva: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editarReserva(
        id: String,
        nombre: String,
        comensales: Int,
        notas: String,
        fecha: Long,
        email: String = "",
        telefono: String = ""
    ) {
        val uid = requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "nombreCliente" to nombre.trim(),
                    "comensales" to comensales,
                    "notas" to notas.trim(),
                    "fecha" to fecha,
                    "email" to email.trim(),
                    "telefono" to telefono.trim()
                )
                reservasRef(uid).document(id).update(data).await()
            } catch (e: Exception) {
                _error.value = "Error al editar reserva: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cambiarEstado(id: String, nuevoEstado: String) {
        val uid = requireUid() ?: return
        viewModelScope.launch {
            try {
                reservasRef(uid).document(id).update("estado", nuevoEstado).await()
            } catch (e: Exception) {
                _error.value = "Error al cambiar estado: ${e.message}"
            }
        }
    }

    fun eliminarReserva(id: String) {
        val uid = requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                reservasRef(uid).document(id).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar reserva: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reservasListener?.remove()
    }
}