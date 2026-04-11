package com.example.qtengo.restauracion.ui.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionReserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReservasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _reservas = MutableStateFlow<List<RestauracionReserva>>(emptyList())
    val reservas = _reservas.asStateFlow()

    private fun reservasRef() = db.collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("reservas")

    fun cargarReservas() {
        val user = auth.currentUser ?: return
        reservasRef().addSnapshotListener { snapshot, _ ->
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

    fun agregarReserva(nombre: String, comensales: Int, notas: String) {
        viewModelScope.launch {
            val data = mapOf(
                "nombreCliente" to nombre,
                "comensales" to comensales,
                "notas" to notas,
                "fecha" to System.currentTimeMillis() // Simplificado: hoy
            )
            try { reservasRef().add(data).await() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun eliminarReserva(id: String) {
        viewModelScope.launch {
            try { reservasRef().document(id).delete().await() } catch (e: Exception) { e.printStackTrace() }
        }
    }
}