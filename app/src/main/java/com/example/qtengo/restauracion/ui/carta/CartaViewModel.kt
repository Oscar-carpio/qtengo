package com.example.qtengo.restauracion.ui.carta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Plato(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "Principales",
    val disponible: Boolean = true
)

data class MenuDia(
    val id: String = "",
    val primerPlato: String = "",
    val segundoPlato: String = "",
    val postre: String = "",
    val precio: Double = 0.0,
    val fecha: String = ""
)

class CartaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _platos = MutableStateFlow<List<Plato>>(emptyList())
    val platos: StateFlow<List<Plato>> = _platos

    private val _menuDia = MutableStateFlow<MenuDia?>(null)
    val menuDia: StateFlow<MenuDia?> = _menuDia

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var platosListener: ListenerRegistration? = null
    private var menuListener: ListenerRegistration? = null

    fun clearError() {
        _error.value = null
    }

    private fun obtenerUid(): String? {
        val usuario = auth.currentUser

        if (usuario == null) {
            _error.value = "Usuario no autenticado"
            return null
        }

        return usuario.uid
    }

    private fun referenciaPlatos(uid: String) =
        db.collection("usuarios")
            .document(uid)
            .collection("restauracion_platos")

    private fun referenciaMenu(uid: String) =
        db.collection("usuarios")
            .document(uid)
            .collection("restauracion_menu")

    fun cargarPlatos() {
        val uid = obtenerUid() ?: return

        platosListener?.remove()

        platosListener = referenciaPlatos(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _error.value = "Error al cargar carta: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    _platos.value = emptyList()
                    return@addSnapshotListener
                }

                val lista = mutableListOf<Plato>()

                for (documento in snapshot.documents) {
                    val plato = Plato(
                        id = documento.id,
                        nombre = documento.getString("nombre") ?: "",
                        descripcion = documento.getString("descripcion") ?: "",
                        precio = documento.getDouble("precio") ?: 0.0,
                        categoria = documento.getString("categoria") ?: "Principales",
                        disponible = documento.getBoolean("disponible") ?: true
                    )

                    lista.add(plato)
                }

                _platos.value = lista
            }
    }

    fun cargarMenuDia() {
        val uid = obtenerUid() ?: return

        menuListener?.remove()

        menuListener = referenciaMenu(uid)
            .document("hoy")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _error.value = "Error al cargar menú: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    _menuDia.value = null
                    return@addSnapshotListener
                }

                val menu = MenuDia(
                    id = snapshot.id,
                    primerPlato = snapshot.getString("primerPlato") ?: "",
                    segundoPlato = snapshot.getString("segundoPlato") ?: "",
                    postre = snapshot.getString("postre") ?: "",
                    precio = snapshot.getDouble("precio") ?: 0.0,
                    fecha = snapshot.getString("fecha") ?: ""
                )

                _menuDia.value = menu
            }
    }

    fun añadirPlato(plato: Plato) {
        val uid = obtenerUid() ?: return

        viewModelScope.launch {
            try {
                val datos = hashMapOf(
                    "nombre" to plato.nombre,
                    "descripcion" to plato.descripcion,
                    "precio" to plato.precio,
                    "categoria" to plato.categoria,
                    "disponible" to plato.disponible
                )

                referenciaPlatos(uid)
                    .add(datos)
                    .await()

            } catch (e: Exception) {
                _error.value = "Error al añadir plato: ${e.message}"
            }
        }
    }

    fun editarPlato(
        platoId: String,
        plato: Plato
    ) {
        val uid = obtenerUid() ?: return

        if (platoId.isBlank()) {
            _error.value = "No se puede editar el plato porque no tiene ID"
            return
        }

        viewModelScope.launch {
            try {
                referenciaPlatos(uid)
                    .document(platoId)
                    .update(
                        "nombre", plato.nombre,
                        "descripcion", plato.descripcion,
                        "precio", plato.precio,
                        "categoria", plato.categoria
                    )
                    .await()

            } catch (e: Exception) {
                _error.value = "Error al editar plato: ${e.message}"
            }
        }
    }

    fun toggleDisponible(
        platoId: String,
        disponible: Boolean
    ) {
        val uid = obtenerUid() ?: return

        if (platoId.isBlank()) {
            _error.value = "No se puede actualizar el plato porque no tiene ID"
            return
        }

        viewModelScope.launch {
            try {
                referenciaPlatos(uid)
                    .document(platoId)
                    .update("disponible", disponible)
                    .await()

            } catch (e: Exception) {
                _error.value = "Error al actualizar plato: ${e.message}"
            }
        }
    }

    fun eliminarPlato(platoId: String) {
        val uid = obtenerUid() ?: return

        if (platoId.isBlank()) {
            _error.value = "No se puede eliminar el plato porque no tiene ID"
            return
        }

        viewModelScope.launch {
            try {
                referenciaPlatos(uid)
                    .document(platoId)
                    .delete()
                    .await()

            } catch (e: Exception) {
                _error.value = "Error al eliminar plato: ${e.message}"
            }
        }
    }

    fun guardarMenuDia(menu: MenuDia) {
        val uid = obtenerUid() ?: return

        viewModelScope.launch {
            try {
                val datos = hashMapOf(
                    "primerPlato" to menu.primerPlato,
                    "segundoPlato" to menu.segundoPlato,
                    "postre" to menu.postre,
                    "precio" to menu.precio,
                    "fecha" to menu.fecha
                )

                referenciaMenu(uid)
                    .document("hoy")
                    .set(datos)
                    .await()

            } catch (e: Exception) {
                _error.value = "Error al guardar menú: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        platosListener?.remove()
        menuListener?.remove()
    }
}