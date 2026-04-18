package com.example.qtengo.restauracion.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionProducto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InventarioRestauracionViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _items = MutableStateFlow<List<RestauracionProducto>>(emptyList())
    val items: StateFlow<List<RestauracionProducto>> = _items.asStateFlow()

    private fun inventarioRef(uid: String) =
        db.collection("usuarios")
            .document(uid)
            .collection("inventarioRestauracion")

    fun cargarItems() {
        val user = auth.currentUser
        if (user == null) {
            println("No hay usuario autenticado, no se pueden cargar productos")
            _items.value = emptyList()
            return
        }

        inventarioRef(user.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error al obtener productos: ${error.message}")
                _items.value = emptyList()
                return@addSnapshotListener
            }

            val lista = snapshot?.documents?.map { doc ->
                RestauracionProducto(
                    id_producto = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    categoria = doc.getString("categoria") ?: "",
                    stock = (doc.getLong("stock") ?: 0L).toInt(),
                    stock_minimo = (doc.getLong("stock_minimo") ?: 0L).toInt(),
                    precio = doc.getDouble("precio") ?: 0.0
                )
            } ?: emptyList()

            _items.value = lista
        }
    }

    fun agregarItem(
        nombre: String,
        categoria: String,
        stock: Int,
        stockMinimo: Int,
        precio: Double
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede agregar producto")
                return@launch
            }

            val data = hashMapOf(
                "nombre" to nombre.trim(),
                "categoria" to categoria.trim(),
                "stock" to stock,
                "stock_minimo" to stockMinimo,
                "precio" to precio
            )

            try {
                inventarioRef(user.uid).add(data).await()
            } catch (e: Exception) {
                println("Error al agregar producto: ${e.message}")
            }
        }
    }

    fun eliminarItem(idProducto: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede eliminar producto")
                return@launch
            }

            try {
                inventarioRef(user.uid)
                    .document(idProducto)
                    .delete()
                    .await()
            } catch (e: Exception) {
                println("Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun actualizarStock(idProducto: String, nuevoStock: Int) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede actualizar stock")
                return@launch
            }

            try {
                inventarioRef(user.uid)
                    .document(idProducto)
                    .update("stock", nuevoStock.coerceAtLeast(0))
                    .await()
            } catch (e: Exception) {
                println("Error al actualizar stock: ${e.message}")
            }
        }
    }

    fun aumentarStock(producto: RestauracionProducto) {
        actualizarStock(
            idProducto = producto.id_producto,
            nuevoStock = producto.stock + 1
        )
    }

    fun disminuirStock(producto: RestauracionProducto) {
        actualizarStock(
            idProducto = producto.id_producto,
            nuevoStock = (producto.stock - 1).coerceAtLeast(0)
        )
    }
}