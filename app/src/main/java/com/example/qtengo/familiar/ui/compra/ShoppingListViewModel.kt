package com.example.qtengo.familiar.ui.compra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val itemCount: Int = 0,
    val date: String = ""
)

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val price: Double = 0.0,        // FIX WARN — antes era String
    val isChecked: Boolean = false
)

data class FavoriteItem(
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val price: Double = 0.0         // FIX WARN — antes era String
)

class ShoppingListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items

    private val _favoritos = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val favoritos: StateFlow<List<FavoriteItem>> = _favoritos

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var listasListener: ListenerRegistration? = null
    private var itemsListener: ListenerRegistration? = null
    private var favoritosListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private fun requireUid(): String? {
        if (uid.isBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return uid
    }

    private fun listasRef() = db.collection("usuarios").document(uid).collection("listas")
    private fun favoritosRef() = db.collection("usuarios").document(uid).collection("favoritos")

    // ─── Listas ──────────────────────────────────────────────────────────────

    fun cargarListas() {
        val uid = requireUid() ?: return
        listasListener?.remove()
        listasListener = db.collection("usuarios").document(uid).collection("listas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar listas: ${e.message}"
                    return@addSnapshotListener
                }
                _lists.value = snapshot?.documents?.map { doc ->
                    ShoppingList(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        itemCount = (doc.getLong("itemCount") ?: 0).toInt(),
                        date = doc.getString("date") ?: ""
                    )
                } ?: emptyList()
            }
    }

    fun cargarItems(listaId: String) {
        val uid = requireUid() ?: return
        itemsListener?.remove()
        itemsListener = db.collection("usuarios").document(uid)
            .collection("listas").document(listaId).collection("productos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar productos: ${e.message}"
                    return@addSnapshotListener
                }
                _items.value = snapshot?.documents?.map { doc ->
                    ShoppingItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        quantity = doc.getString("quantity") ?: "",
                        price = doc.getDouble("price") ?: 0.0,   // FIX WARN — leemos Double
                        isChecked = doc.getBoolean("isChecked") ?: false
                    )
                } ?: emptyList()
            }
    }

    fun cargarFavoritos() {
        val uid = requireUid() ?: return
        favoritosListener?.remove()
        favoritosListener = db.collection("usuarios").document(uid).collection("favoritos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar favoritos: ${e.message}"
                    return@addSnapshotListener
                }
                _favoritos.value = snapshot?.documents?.map { doc ->
                    FavoriteItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        quantity = doc.getString("quantity") ?: "",
                        price = doc.getDouble("price") ?: 0.0    // FIX WARN — leemos Double
                    )
                } ?: emptyList()
            }
    }

    // ─── Favoritos ───────────────────────────────────────────────────────────

    /**
     * Guarda un producto como favorito.
     * FIX WARN — comprueba si ya existe un favorito con el mismo nombre antes de añadir.
     */
    fun guardarFavorito(nombre: String, cantidad: String, precio: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // FIX WARN #2 — evitar duplicados
                val existente = favoritosRef()
                    .whereEqualTo("name", nombre.trim())
                    .get()
                    .await()

                if (!existente.isEmpty) {
                    _error.value = "\"$nombre\" ya está en tus favoritos"
                    return@launch
                }

                val data = mapOf(
                    "name" to nombre.trim(),
                    "quantity" to cantidad,
                    "price" to precio           // FIX WARN — guardamos Double
                )
                favoritosRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al guardar favorito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarFavorito(favoritoId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                favoritosRef().document(favoritoId).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar favorito: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun añadirFavoritoALista(listaId: String, favorito: FavoriteItem) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "name" to favorito.name,
                    "quantity" to favorito.quantity.ifBlank { "1" },
                    "price" to favorito.price,  // FIX WARN — Double
                    "isChecked" to false
                )
                listasRef().document(listaId).collection("productos").add(data).await()
                actualizarContador(listaId, delta = 1)
            } catch (e: Exception) {
                _error.value = "Error al añadir favorito a la lista: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─── Listas ──────────────────────────────────────────────────────────────

    fun crearLista(nombre: String) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date())
                val data = mapOf(
                    "name" to nombre,
                    "itemCount" to 0,
                    "date" to fecha
                )
                listasRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al crear la lista: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─── Productos ───────────────────────────────────────────────────────────

    /** precio recibe Double — validado y parseado en el diálogo antes de llegar aquí */
    fun añadirItem(listaId: String, nombre: String, cantidad: String, precio: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "name" to nombre,
                    "quantity" to cantidad.ifBlank { "1" },
                    "price" to precio,          // FIX WARN — Double
                    "isChecked" to false
                )
                listasRef().document(listaId).collection("productos").add(data).await()
                actualizarContador(listaId, delta = 1)
            } catch (e: Exception) {
                _error.value = "Error al añadir producto: ${e.message}"
            }
            finally { _isLoading.value = false }
        }
    }

    /** precio recibe Double — validado y parseado en el diálogo antes de llegar aquí */
    fun editarItem(listaId: String, itemId: String, nombre: String, cantidad: String, precio: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = mapOf(
                    "name" to nombre,
                    "quantity" to cantidad,
                    "price" to precio           // FIX WARN — Double
                )
                listasRef().document(listaId)
                    .collection("productos").document(itemId)
                    .update(data).await()
            } catch (e: Exception) {
                _error.value = "Error al editar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleItem(listaId: String, itemId: String, checked: Boolean) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                listasRef().document(listaId)
                    .collection("productos").document(itemId)
                    .update("isChecked", checked).await()
            } catch (e: Exception) {
                _error.value = "Error al actualizar producto: ${e.message}"
            }finally { _isLoading.value = false }

        }
    }

    fun eliminarLista(listaId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productos = listasRef().document(listaId)
                    .collection("productos").get().await()
                val batch = db.batch()
                productos.documents.forEach { batch.delete(it.reference) }
                batch.delete(listasRef().document(listaId))
                batch.commit().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar la lista: ${e.message}"
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarItem(listaId: String, itemId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                listasRef().document(listaId)
                    .collection("productos").document(itemId)
                    .delete().await()
                actualizarContador(listaId, delta = -1)
            } catch (e: Exception) {
                _error.value = "Error al eliminar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun actualizarContador(listaId: String, delta: Int) {
        try {
            listasRef().document(listaId)
                .update("itemCount", FieldValue.increment(delta.toLong()))
                .await()
        } catch (e: Exception) {
            _error.value = "Error al actualizar contador: ${e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        listasListener?.remove()
        itemsListener?.remove()
        favoritosListener?.remove()
    }
}
