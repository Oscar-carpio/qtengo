package com.example.qtengo.restauracion.ui.proveedores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.RestauracionProveedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProveedoresRestauracionViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _proveedores = MutableStateFlow<List<RestauracionProveedor>>(emptyList())
    val proveedores: StateFlow<List<RestauracionProveedor>> = _proveedores

    private fun proveedoresRef(uid: String) =
        db.collection("usuarios").document(uid).collection("proveedoresRestauracion")

    fun cargarProveedores() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            println("No hay usuario autenticado, no se pueden cargar proveedores")
            _proveedores.value = emptyList()
            return
        }

        proveedoresRef(user.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error al obtener proveedores: ${error.message}")
                _proveedores.value = emptyList()
                return@addSnapshotListener
            }

            val result = snapshot?.documents?.map { doc ->
                RestauracionProveedor(
                    id_proveedor = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    email = doc.getString("email") ?: "",
                    telefono = doc.getString("telefono") ?: ""
                )
            } ?: emptyList()

            _proveedores.value = result
        }
    }

    fun agregarProveedor(
        nombre: String,
        telefono: String,
        email: String,
        direccion: String
    ) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede agregar proveedor")
                return@launch
            }

            val data = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "email" to email,
                "direccion" to direccion
            )

            try {
                proveedoresRef(user.uid).add(data).await()
            } catch (e: Exception) {
                println("Error al agregar proveedor: ${e.message}")
            }
        }
    }

    fun eliminarProveedor(id: String) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                println("No hay usuario autenticado, no se puede eliminar proveedor")
                return@launch
            }

            try {
                proveedoresRef(user.uid).document(id).delete().await()
            } catch (e: Exception) {
                println("Error al eliminar proveedor: ${e.message}")
            }
        }
    }
}