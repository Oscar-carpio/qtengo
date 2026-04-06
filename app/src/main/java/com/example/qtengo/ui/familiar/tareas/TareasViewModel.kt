package com.example.qtengo.ui.familiar.tareas

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

data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val completada: Boolean = false,
    val prioridad: String = "Media"
)

class TareasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    /** Referencia base de tareas en Firestore */
    private fun tareasRef() = db.collection("usuarios").document(uid).collection("tareas")

    /** Carga todas las tareas del usuario en tiempo real */
    fun cargarTareas() {
        tareasRef().addSnapshotListener { snapshot, _ ->
            val result = snapshot?.documents?.map { doc ->
                Tarea(
                    id = doc.id,
                    titulo = doc.getString("titulo") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    fecha = doc.getString("fecha") ?: "",
                    completada = doc.getBoolean("completada") ?: false,
                    prioridad = doc.getString("prioridad") ?: "Media"
                )
            } ?: emptyList()
            // Ordenar: pendientes primero, luego por prioridad
            _tareas.value = result.sortedWith(
                compareBy<Tarea> { it.completada }
                    .thenBy {
                        when (it.prioridad) {
                            "Alta" -> 0
                            "Media" -> 1
                            "Baja" -> 2
                            else -> 3
                        }
                    }
            )
        }
    }

    /** Añade una nueva tarea */
    fun añadirTarea(
        titulo: String,
        descripcion: String,
        fecha: String,
        prioridad: String
    ) {
        viewModelScope.launch {
            val data = mapOf(
                "titulo" to titulo,
                "descripcion" to descripcion,
                "fecha" to fecha,
                "completada" to false,
                "prioridad" to prioridad
            )
            tareasRef().add(data).await()
        }
    }

    /** Edita una tarea existente */
    fun editarTarea(
        tareaId: String,
        titulo: String,
        descripcion: String,
        fecha: String,
        prioridad: String
    ) {
        viewModelScope.launch {
            tareasRef().document(tareaId).update(
                mapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "fecha" to fecha,
                    "prioridad" to prioridad
                )
            ).await()
        }
    }

    /** Marca o desmarca una tarea como completada */
    fun toggleTarea(tareaId: String, completada: Boolean) {
        viewModelScope.launch {
            tareasRef().document(tareaId).update("completada", completada).await()
        }
    }

    /** Elimina una tarea */
    fun eliminarTarea(tareaId: String) {
        viewModelScope.launch {
            tareasRef().document(tareaId).delete().await()
        }
    }
}