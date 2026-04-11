package com.example.qtengo.restauracion.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionPlato
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MenuViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _platos = MutableStateFlow<List<RestauracionPlato>>(emptyList())
    val platos = _platos.asStateFlow()

    private fun menuRef() = db.collection("usuarios")
        .document(auth.currentUser?.uid ?: "").collection("menu")

    fun cargarMenu() {
        menuRef().addSnapshotListener { snap, _ ->
            _platos.value = snap?.documents?.map { doc ->
                RestauracionPlato(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    precio = doc.getDouble("precio") ?: 0.0,
                    disponible = doc.getBoolean("disponible") ?: true
                )
            } ?: emptyList()
        }
    }

    fun agregarPlato(nombre: String, precio: Double) {
        viewModelScope.launch {
            val data = mapOf("nombre" to nombre, "precio" to precio, "disponible" to true)
            menuRef().add(data)
        }
    }

    fun eliminarPlato(id: String) {
        viewModelScope.launch { menuRef().document(id).delete() }
    }
}