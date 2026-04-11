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
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _platos = MutableStateFlow<List<Plato>>(emptyList())
    val platos: StateFlow<List<Plato>> = _platos

    private val _menuDia = MutableStateFlow<MenuDia?>(null)
    val menuDia: StateFlow<MenuDia?> = _menuDia

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var platosListener: ListenerRegistration? = null
    private var menuListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private fun requireUid(): String? {
        if (uid.isBlank()) { _error.value = "Usuario no autenticado"; return null }
        return uid
    }

    private fun platosRef() = db.collection("usuarios").document(uid).collection("restauracion_platos")
    private fun menuRef() = db.collection("usuarios").document(uid).collection("restauracion_menu")

    fun cargarPlatos() {
        val uid = requireUid() ?: return
        platosListener?.remove()
        platosListener = db.collection("usuarios").document(uid).collection("restauracion_platos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _error.value = "Error al cargar carta: ${e.message}"; return@addSnapshotListener }
                _platos.value = snapshot?.documents?.map { doc ->
                    Plato(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        categoria = doc.getString("categoria") ?: "Principales",
                        disponible = doc.getBoolean("disponible") ?: true
                    )
                } ?: emptyList()
            }
    }

    fun cargarMenuDia() {
        val uid = requireUid() ?: return
        menuListener?.remove()
        menuListener = db.collection("usuarios").document(uid).collection("restauracion_menu")
            .document("hoy")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _error.value = "Error al cargar menú: ${e.message}"; return@addSnapshotListener }
                _menuDia.value = snapshot?.let { doc ->
                    if (!doc.exists()) return@let null
                    MenuDia(
                        id = doc.id,
                        primerPlato = doc.getString("primerPlato") ?: "",
                        segundoPlato = doc.getString("segundoPlato") ?: "",
                        postre = doc.getString("postre") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        fecha = doc.getString("fecha") ?: ""
                    )
                }
            }
    }

    fun añadirPlato(plato: Plato) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to plato.nombre,
                    "descripcion" to plato.descripcion,
                    "precio" to plato.precio,
                    "categoria" to plato.categoria,
                    "disponible" to plato.disponible
                )
                platosRef().add(data).await()
            } catch (e: Exception) { _error.value = "Error al añadir plato: ${e.message}" }
        }
    }

    /** Edita un plato existente de la carta */
    fun editarPlato(platoId: String, plato: Plato) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                platosRef().document(platoId).update(
                    "nombre", plato.nombre,
                    "descripcion", plato.descripcion,
                    "precio", plato.precio,
                    "categoria", plato.categoria
                ).await()
            } catch (e: Exception) { _error.value = "Error al editar plato: ${e.message}" }
        }
    }

    fun toggleDisponible(platoId: String, disponible: Boolean) {
        requireUid() ?: return
        viewModelScope.launch {
            try { platosRef().document(platoId).update("disponible", disponible).await() }
            catch (e: Exception) { _error.value = "Error al actualizar plato: ${e.message}" }
        }
    }

    fun eliminarPlato(platoId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            try { platosRef().document(platoId).delete().await() }
            catch (e: Exception) { _error.value = "Error al eliminar plato: ${e.message}" }
        }
    }

    fun guardarMenuDia(menu: MenuDia) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "primerPlato" to menu.primerPlato,
                    "segundoPlato" to menu.segundoPlato,
                    "postre" to menu.postre,
                    "precio" to menu.precio,
                    "fecha" to menu.fecha
                )
                menuRef().document("hoy").set(data).await()
            } catch (e: Exception) { _error.value = "Error al guardar menú: ${e.message}" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        platosListener?.remove()
        menuListener?.remove()
    }
}