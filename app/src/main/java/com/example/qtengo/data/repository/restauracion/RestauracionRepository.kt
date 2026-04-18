package com.example.qtengo.data.repository.restauracion

import android.util.Log
import com.example.qtengo.data.model.restauracion.RestauracionMovimiento
import com.example.qtengo.data.model.restauracion.RestauracionPedido
import com.example.qtengo.data.model.restauracion.RestauracionPlato
import com.example.qtengo.data.model.restauracion.RestauracionProducto
import com.example.qtengo.data.model.restauracion.RestauracionProveedor
import com.example.qtengo.data.model.restauracion.RestauracionReserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RestauracionRepository {

    companion object {
        private const val TAG = "RestauracionRepo"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Función auxiliar para obtener el UID del usuario actual
    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
    }

    // --- Productos ---
    suspend fun getProductos(): List<RestauracionProducto> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/productos")

            val snapshot = firestore.collection("usuarios").document(uid).collection("productos").get().await()
            val lista = snapshot.toObjects(RestauracionProducto::class.java)

            Log.d(TAG, "Productos obtenidos: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener productos", e)
            emptyList()
        }
    }

    suspend fun addProducto(producto: RestauracionProducto) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/productos")

            val docRef = firestore.collection("usuarios").document(uid).collection("productos").document()
            val nuevoProducto = producto.copy(id_producto = docRef.id)

            docRef.set(nuevoProducto).await()
            Log.d(TAG, "Producto guardado correctamente en Firestore -> $nuevoProducto")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar producto", e)
        }
    }

    // --- Proveedores ---
    suspend fun getProveedores(): List<RestauracionProveedor> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/proveedores")

            val snapshot = firestore.collection("usuarios").document(uid).collection("proveedores").get().await()
            val lista = snapshot.toObjects(RestauracionProveedor::class.java)

            Log.d(TAG, "Proveedores obtenidos: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener proveedores", e)
            emptyList()
        }
    }

    suspend fun addProveedor(proveedor: RestauracionProveedor) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/proveedores")

            val docRef = firestore.collection("usuarios").document(uid).collection("proveedores").document()
            val nuevoProveedor = proveedor.copy(id_proveedor = docRef.id)

            docRef.set(nuevoProveedor).await()
            Log.d(TAG, "Proveedor guardado correctamente en Firestore -> $nuevoProveedor")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar proveedor", e)
        }
    }

    // --- Pedidos ---
    suspend fun getPedidos(): List<RestauracionPedido> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/pedidos")

            val snapshot = firestore.collection("usuarios").document(uid).collection("pedidos").get().await()
            val lista = snapshot.toObjects(RestauracionPedido::class.java)

            Log.d(TAG, "Pedidos obtenidos: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener pedidos", e)
            emptyList()
        }
    }

    suspend fun addPedido(pedido: RestauracionPedido) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/pedidos")

            val docRef = firestore.collection("usuarios").document(uid).collection("pedidos").document()
            val nuevoPedido = pedido.copy(id_pedido = docRef.id)

            docRef.set(nuevoPedido).await()
            Log.d(TAG, "Pedido guardado correctamente en Firestore -> $nuevoPedido")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar pedido", e)
        }
    }

    // --- Movimientos ---
    suspend fun getMovimientos(): List<RestauracionMovimiento> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/movimientos")

            val snapshot = firestore.collection("usuarios").document(uid).collection("movimientos").get().await()
            val lista = snapshot.toObjects(RestauracionMovimiento::class.java)

            Log.d(TAG, "Movimientos obtenidos: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener movimientos", e)
            emptyList()
        }
    }

    suspend fun addMovimiento(movimiento: RestauracionMovimiento) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/movimientos")

            val docRef = firestore.collection("usuarios").document(uid).collection("movimientos").document()
            val nuevoMovimiento = movimiento.copy(id_movimiento = docRef.id)

            docRef.set(nuevoMovimiento).await()
            Log.d(TAG, "Movimiento guardado correctamente en Firestore -> $nuevoMovimiento")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar movimiento", e)
        }
    }

    // --- Reservas ---
    suspend fun getReservas(): List<RestauracionReserva> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/reservas")
            val snapshot = firestore.collection("usuarios").document(uid).collection("reservas").get().await()
            val lista = snapshot.toObjects(RestauracionReserva::class.java)
            Log.d(TAG, "Reservas obtenidas: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reservas", e)
            emptyList()
        }
    }

    suspend fun addReserva(reserva: RestauracionReserva) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/reservas")
            val docRef = firestore.collection("usuarios").document(uid).collection("reservas").document()
            val nueva = reserva.copy(id = docRef.id)
            docRef.set(nueva).await()
            Log.d(TAG, "Reserva guardada correctamente en Firestore -> $nueva")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar reserva", e)
        }
    }

    suspend fun eliminarReserva(id: String) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Eliminando reserva: $id en usuarios/$uid/reservas")
            firestore.collection("usuarios").document(uid).collection("reservas").document(id).delete().await()
            Log.d(TAG, "Reserva eliminada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar reserva", e)
        }
    }

    // --- Menú / Carta ---
    suspend fun getPlatos(): List<RestauracionPlato> {
        return try {
            val uid = getUserId()
            Log.d(TAG, "Leyendo colección: usuarios/$uid/menu")
            val snapshot = firestore.collection("usuarios").document(uid).collection("menu").get().await()
            val lista = snapshot.toObjects(RestauracionPlato::class.java)
            Log.d(TAG, "Platos obtenidos: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener platos", e)
            emptyList()
        }
    }

    suspend fun addPlato(plato: RestauracionPlato) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Insertando en colección: usuarios/$uid/menu")
            val docRef = firestore.collection("usuarios").document(uid).collection("menu").document()
            val nuevo = plato.copy(id = docRef.id)
            docRef.set(nuevo).await()
            Log.d(TAG, "Plato guardado correctamente en Firestore -> $nuevo")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar plato", e)
        }
    }

    suspend fun eliminarPlato(id: String) {
        try {
            val uid = getUserId()
            Log.d(TAG, "Eliminando plato: $id en usuarios/$uid/menu")
            firestore.collection("usuarios").document(uid).collection("menu").document(id).delete().await()
            Log.d(TAG, "Plato eliminado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar plato", e)
        }
    }
}