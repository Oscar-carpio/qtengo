package com.example.qtengo.familiar.ui.compra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    val price: String = "",
    val isChecked: Boolean = false
)

data class FavoriteItem(
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val price: String = ""
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

    /** Referencia base del usuario en Firestore */
    private fun listasRef() = db.collection("usuarios").document(uid).collection("listas")

    /** Referencia a la colección de favoritos del usuario */
    private fun favoritosRef() = db.collection("usuarios").document(uid).collection("favoritos")

    /** Carga todas las listas del usuario en tiempo real */
    fun cargarListas() {
        listasRef().addSnapshotListener { snapshot, _ ->
            val result = snapshot?.documents?.map { doc ->
                ShoppingList(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    itemCount = (doc.getLong("itemCount") ?: 0).toInt(),
                    date = doc.getString("date") ?: ""
                )
            } ?: emptyList()
            _lists.value = result
        }
    }

    /** Carga los productos de una lista en tiempo real */
    fun cargarItems(listaId: String) {
        listasRef().document(listaId).collection("productos")
            .addSnapshotListener { snapshot, _ ->
                val result = snapshot?.documents?.map { doc ->
                    ShoppingItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        quantity = doc.getString("quantity") ?: "",
                        price = doc.getString("price") ?: "",
                        isChecked = doc.getBoolean("isChecked") ?: false
                    )
                } ?: emptyList()
                _items.value = result
            }
    }

    /** Carga los productos favoritos del usuario en tiempo real */
    fun cargarFavoritos() {
        favoritosRef().addSnapshotListener { snapshot, _ ->
            val result = snapshot?.documents?.map { doc ->
                FavoriteItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    quantity = doc.getString("quantity") ?: "",
                    price = doc.getString("price") ?: ""
                )
            } ?: emptyList()
            _favoritos.value = result
        }
    }

    /** Guarda un producto como favorito */
    fun guardarFavorito(nombre: String, cantidad: String, precio: String) {
        viewModelScope.launch {
            val data = mapOf(
                "name" to nombre,
                "quantity" to cantidad,
                "price" to precio
            )
            favoritosRef().add(data).await()
        }
    }

    /** Elimina un favorito */
    fun eliminarFavorito(favoritoId: String) {
        viewModelScope.launch {
            favoritosRef().document(favoritoId).delete().await()
        }
    }

    /** Añade un favorito directamente a una lista */
    fun añadirFavoritoALista(listaId: String, favorito: FavoriteItem) {
        viewModelScope.launch {
            val data = mapOf(
                "name" to favorito.name,
                "quantity" to favorito.quantity.ifBlank { "1" },
                "price" to favorito.price,
                "isChecked" to false
            )
            listasRef().document(listaId).collection("productos").add(data).await()
            actualizarContador(listaId)
        }
    }

    /** Crea una nueva lista de la compra */
    fun crearLista(nombre: String) {
        viewModelScope.launch {
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val data = mapOf(
                "name" to nombre,
                "itemCount" to 0,
                "date" to fecha
            )
            listasRef().add(data).await()
        }
    }

    /** Añade un producto a una lista */
    fun añadirItem(listaId: String, nombre: String, cantidad: String, precio: String) {
        viewModelScope.launch {
            val data = mapOf(
                "name" to nombre,
                "quantity" to cantidad.ifBlank { "1" },
                "price" to precio,
                "isChecked" to false
            )
            listasRef().document(listaId).collection("productos").add(data).await()
            actualizarContador(listaId)
        }
    }

    /** Edita un producto existente de una lista */
    fun editarItem(listaId: String, itemId: String, nombre: String, cantidad: String, precio: String) {
        viewModelScope.launch {
            val data = mapOf(
                "name" to nombre,
                "quantity" to cantidad,
                "price" to precio
            )
            listasRef().document(listaId)
                .collection("productos").document(itemId)
                .update(data).await()
        }
    }

    /** Marca o desmarca un producto como comprado */
    fun toggleItem(listaId: String, itemId: String, checked: Boolean) {
        viewModelScope.launch {
            listasRef().document(listaId)
                .collection("productos").document(itemId)
                .update("isChecked", checked).await()
        }
    }

    /** Elimina una lista y todos sus productos */
    fun eliminarLista(listaId: String) {
        viewModelScope.launch {
            val productos = listasRef().document(listaId)
                .collection("productos").get().await()
            productos.documents.forEach { it.reference.delete().await() }
            listasRef().document(listaId).delete().await()
        }
    }

    /** Elimina un producto de una lista */
    fun eliminarItem(listaId: String, itemId: String) {
        viewModelScope.launch {
            listasRef().document(listaId)
                .collection("productos").document(itemId)
                .delete().await()
            actualizarContador(listaId)
        }
    }

    /** Actualiza el contador de productos de una lista */
    private suspend fun actualizarContador(listaId: String) {
        val count = listasRef().document(listaId)
            .collection("productos").get().await().size()
        listasRef().document(listaId).update("itemCount", count).await()
    }
}