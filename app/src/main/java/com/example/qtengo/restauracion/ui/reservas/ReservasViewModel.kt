package com.example.qtengo.restauracion.ui.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Reserva(
    val id: String = "",
    val nombreCliente: String = "",
    val telefono: String = "",
    val email: String = "",          // nuevo campo
    val fecha: String = "",
    val hora: String = "",
    val comensales: Int = 2,
    val mesa: String = "",
    val estado: String = "Confirmada",
    val notas: String = ""
)

class ReservasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var reservasListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private fun requireUid(): String? {
        if (uid.isBlank()) { _error.value = "Usuario no autenticado"; return null }
        return uid
    }

    private fun reservasRef() = db.collection("usuarios").document(uid).collection("restauracion_reservas")

    fun cargarReservas() {
        val uid = requireUid() ?: return
        reservasListener?.remove()
        reservasListener = db.collection("usuarios").document(uid).collection("restauracion_reservas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _error.value = "Error al cargar reservas: ${e.message}"; return@addSnapshotListener }
                _reservas.value = snapshot?.documents?.map { doc ->
                    Reserva(
                        id = doc.id,
                        nombreCliente = doc.getString("nombreCliente") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        email = doc.getString("email") ?: "",
                        fecha = doc.getString("fecha") ?: "",
                        hora = doc.getString("hora") ?: "",
                        comensales = (doc.getLong("comensales") ?: 2).toInt(),
                        mesa = doc.getString("mesa") ?: "",
                        estado = doc.getString("estado") ?: "Confirmada",
                        notas = doc.getString("notas") ?: ""
                    )
                }?.sortedWith(compareBy({ it.fecha }, { it.hora })) ?: emptyList()
            }
    }

    fun añadirReserva(reserva: Reserva) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombreCliente" to reserva.nombreCliente,
                    "telefono" to reserva.telefono,
                    "email" to reserva.email,
                    "fecha" to reserva.fecha,
                    "hora" to reserva.hora,
                    "comensales" to reserva.comensales,
                    "mesa" to reserva.mesa,
                    "estado" to reserva.estado,
                    "notas" to reserva.notas
                )
                reservasRef().add(data).await()
            } catch (e: Exception) { _error.value = "Error al añadir reserva: ${e.message}" }
        }
    }

    fun actualizarEstado(reservaId: String, estado: String) {
        requireUid() ?: return
        viewModelScope.launch {
            try { reservasRef().document(reservaId).update("estado", estado).await() }
            catch (e: Exception) { _error.value = "Error al actualizar reserva: ${e.message}" }
        }
    }

    fun eliminarReserva(reservaId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            try { reservasRef().document(reservaId).delete().await() }
            catch (e: Exception) { _error.value = "Error al eliminar reserva: ${e.message}" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reservasListener?.remove()
    }
}