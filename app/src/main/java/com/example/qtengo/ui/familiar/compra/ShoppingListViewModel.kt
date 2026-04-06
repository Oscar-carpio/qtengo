package com.example.qtengo.ui.familiar.compra

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
    val isChecked: Boolean = false
)

class ShoppingListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items

    /** Referencia base del usuario en Firestore */
    private fun listasRef() = db.collection("usuarios").document(uid).collection("listas")

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
                        isChecked = doc.getBoolean("isChecked") ?: false
                    )
                } ?: emptyList()
                _items.value = result
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
    fun añadirItem(listaId: String, nombre: String, cantidad: String) {
        viewModelScope.launch {
            val data = mapOf(
                "name" to nombre,
                "quantity" to cantidad.ifBlank { "1 unidad" },
                "isChecked" to false
            )
            listasRef().document(listaId).collection("productos").add(data).await()
            actualizarContador(listaId)
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