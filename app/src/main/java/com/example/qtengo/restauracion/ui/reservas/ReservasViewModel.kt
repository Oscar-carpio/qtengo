package com.example.qtengo.restauracion.ui.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionReserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReservasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var reservasListener: ListenerRegistration? = null

    private val _reservas = MutableStateFlow<List<RestauracionReserva>>(emptyList())
    val reservas = _reservas.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    val reservasFiltradas: StateFlow<List<RestauracionReserva>> =
        combine(_reservas, _filtro) { lista, texto ->
            val filtroLimpio = texto.trim()

            if (filtroLimpio.isBlank()) {
                lista
            } else {
                lista.filter { reserva ->
                    reserva.nombreCliente.contains(filtroLimpio, ignoreCase = true) ||
                            reserva.notas.contains(filtroLimpio, ignoreCase = true) ||
                            reserva.comensales.toString().contains(filtroLimpio)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun reservasRef() = db.collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("reservas")

    fun actualizarFiltro(valor: String) {
        _filtro.value = valor
    }

    fun cargarReservas() {
        val user = auth.currentUser
        if (user == null) {
            println("No hay usuario autenticado, no se pueden cargar reservas")
            _reservas.value = emptyList()
            return
        }

        reservasListener?.remove()

        reservasListener = reservasRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error al cargar reservas: ${error.message}")
                _reservas.value = emptyList()
                return@addSnapshotListener
            }

            val lista = snapshot?.documents?.map { doc ->
                RestauracionReserva(
                    id = doc.id,
                    nombreCliente = doc.getString("nombreCliente") ?: "",
                    fecha = doc.getLong("fecha") ?: 0L,
                    comensales = (doc.getLong("comensales") ?: 0L).toInt(),
                    notas = doc.getString("notas") ?: ""
                )
            } ?: emptyList()

            _reservas.value = lista.sortedBy { it.fecha }
        }
    }

    fun agregarReserva(
        nombre: String,
        comensales: Int,
        notas: String,
        fecha: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede agregar reserva")
                return@launch
            }

            val data = mapOf(
                "nombreCliente" to nombre.trim(),
                "comensales" to comensales,
                "notas" to notas.trim(),
                "fecha" to fecha
            )

            try {
                reservasRef().add(data).await()
            } catch (e: Exception) {
                println("Error al agregar reserva: ${e.message}")
            }
        }
    }

    fun editarReserva(
        id: String,
        nombre: String,
        comensales: Int,
        notas: String,
        fecha: Long
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede editar reserva")
                return@launch
            }

            val data = mapOf(
                "nombreCliente" to nombre.trim(),
                "comensales" to comensales,
                "notas" to notas.trim(),
                "fecha" to fecha
            )

            try {
                reservasRef().document(id).update(data).await()
            } catch (e: Exception) {
                println("Error al editar reserva: ${e.message}")
            }
        }
    }

    fun eliminarReserva(id: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede eliminar reserva")
                return@launch
            }

            try {
                reservasRef().document(id).delete().await()
            } catch (e: Exception) {
                println("Error al eliminar reserva: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reservasListener?.remove()
    }
}