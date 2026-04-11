package com.example.qtengo.familiar.ui.tareas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val completada: Boolean = false,
    val prioridad: String = "Media"
)

class TareasViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val workManager = WorkManager.getInstance(application)

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    // FIX CRIT #2 — Canal de errores para la UI
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // FIX CRIT #1 — Guardamos el listener para cancelarlo en onCleared()
    private var tareasListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    // FIX CRIT #3 — Guard centralizado contra uid vacío
    private fun requireUid(): String? {
        if (uid.isBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return uid
    }

    private fun tareasRef() = db.collection("usuarios").document(uid).collection("tareas")

    // ─── Carga de datos ──────────────────────────────────────────────────────

    fun cargarTareas() {
        val uid = requireUid() ?: return

        // FIX CRIT #1 — cancelamos listener anterior antes de suscribirse
        tareasListener?.remove()
        tareasListener = db.collection("usuarios").document(uid).collection("tareas")
            .addSnapshotListener { snapshot, e ->
                // FIX CRIT #2 — capturamos error del listener
                if (e != null) {
                    _error.value = "Error al cargar tareas: ${e.message}"
                    return@addSnapshotListener
                }
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

    // ─── Escritura ───────────────────────────────────────────────────────────

    fun añadirTarea(titulo: String, descripcion: String, fecha: String, prioridad: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                val data = mapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "fecha" to fecha,
                    "completada" to false,
                    "prioridad" to prioridad
                )
                val docRef = tareasRef().add(data).await()
                programarNotificacion(docRef.id, titulo, descripcion, fecha)
            } catch (e: Exception) {
                _error.value = "Error al añadir tarea: ${e.message}"
            }
        }
    }

    fun editarTarea(tareaId: String, titulo: String, descripcion: String, fecha: String, prioridad: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                tareasRef().document(tareaId).update(
                    mapOf(
                        "titulo" to titulo,
                        "descripcion" to descripcion,
                        "fecha" to fecha,
                        "prioridad" to prioridad
                    )
                ).await()
                cancelarNotificacion(tareaId)
                programarNotificacion(tareaId, titulo, descripcion, fecha)
            } catch (e: Exception) {
                _error.value = "Error al editar tarea: ${e.message}"
            }
        }
    }

    fun toggleTarea(tareaId: String, completada: Boolean) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                tareasRef().document(tareaId).update("completada", completada).await()
                if (completada) cancelarNotificacion(tareaId)
            } catch (e: Exception) {
                _error.value = "Error al actualizar tarea: ${e.message}"
            }
        }
    }

    fun eliminarTarea(tareaId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #2
            try {
                tareasRef().document(tareaId).delete().await()
                cancelarNotificacion(tareaId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar tarea: ${e.message}"
            }
        }
    }

    // ─── Notificaciones ──────────────────────────────────────────────────────

    private fun programarNotificacion(tareaId: String, titulo: String, descripcion: String, fecha: String) {
        if (fecha.isBlank()) return

        // FIX WARN — Locale fijo para evitar fallos de parse en dispositivos no españoles
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        val fechaTarea = runCatching { sdf.parse(fecha) }.getOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            time = fechaTarea
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        if (delay <= 0) {
            // FIX WARN — informamos al usuario en lugar de fallar silenciosamente
            _error.value = "La fecha ya ha pasado — no se programará notificación"
            return
        }

        val inputData = workDataOf(
            TareaNotificationWorker.KEY_TITULO to titulo,
            TareaNotificationWorker.KEY_DESCRIPCION to descripcion,
            TareaNotificationWorker.KEY_TAREA_ID to tareaId   // FIX WARN — pasamos el ID para la notificación
        )

        val workRequest = OneTimeWorkRequestBuilder<TareaNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(tareaId)
            .build()

        workManager.enqueueUniqueWork(
            tareaId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelarNotificacion(tareaId: String) {
        workManager.cancelUniqueWork(tareaId)
    }

    // FIX CRIT #1 — cancelamos el listener al destruirse el ViewModel
    override fun onCleared() {
        super.onCleared()
        tareasListener?.remove()
    }
}