package com.example.qtengo.restauracion.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionPlato
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

class MenuViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var menuListener: ListenerRegistration? = null

    private val _platos = MutableStateFlow<List<RestauracionPlato>>(emptyList())
    val platos = _platos.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    val platosFiltrados: StateFlow<List<RestauracionPlato>> =
        combine(_platos, _filtro) { lista, texto ->
            val filtroLimpio = texto.trim()

            if (filtroLimpio.isBlank()) {
                lista
            } else {
                lista.filter { plato ->
                    plato.nombre.contains(filtroLimpio, ignoreCase = true) ||
                            plato.precio.toString().contains(filtroLimpio)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun menuRef() = db.collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("menu")

    fun actualizarFiltro(valor: String) {
        _filtro.value = valor
    }

    fun cargarMenu() {
        val user = auth.currentUser
        if (user == null) {
            println("No hay usuario autenticado, no se puede cargar el menú")
            _platos.value = emptyList()
            return
        }

        menuListener?.remove()

        menuListener = menuRef().addSnapshotListener { snap, error ->
            if (error != null) {
                println("Error al cargar menú: ${error.message}")
                _platos.value = emptyList()
                return@addSnapshotListener
            }

            _platos.value = snap?.documents?.map { doc ->
                RestauracionPlato(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    precio = doc.getDouble("precio") ?: 0.0,
                    disponible = doc.getBoolean("disponible") ?: true
                )
            }?.sortedBy { it.nombre.lowercase() } ?: emptyList()
        }
    }

    fun agregarPlato(nombre: String, precio: Double) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede agregar plato")
                return@launch
            }

            val data = mapOf(
                "nombre" to nombre.trim(),
                "precio" to precio,
                "disponible" to true
            )

            try {
                menuRef().add(data).await()
            } catch (e: Exception) {
                println("Error al agregar plato: ${e.message}")
            }
        }
    }

    fun editarPlato(
        id: String,
        nombre: String,
        precio: Double,
        disponible: Boolean
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede editar plato")
                return@launch
            }

            val data = mapOf(
                "nombre" to nombre.trim(),
                "precio" to precio,
                "disponible" to disponible
            )

            try {
                menuRef().document(id).update(data).await()
            } catch (e: Exception) {
                println("Error al editar plato: ${e.message}")
            }
        }
    }

    fun eliminarPlato(id: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede eliminar plato")
                return@launch
            }

            try {
                menuRef().document(id).delete().await()
            } catch (e: Exception) {
                println("Error al eliminar plato: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        menuListener?.remove()
    }
}