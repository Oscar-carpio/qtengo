package com.example.qtengo.familiar.ui.tareas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    /** Añade una nueva tarea y programa su notificación */
    fun añadirTarea(titulo: String, descripcion: String, fecha: String, prioridad: String) {
        viewModelScope.launch {
            val data = mapOf(
                "titulo" to titulo,
                "descripcion" to descripcion,
                "fecha" to fecha,
                "completada" to false,
                "prioridad" to prioridad
            )
            val docRef = tareasRef().add(data).await()
            programarNotificacion(docRef.id, titulo, descripcion, fecha)
        }
    }

    /** Edita una tarea y reprograma su notificación */
    fun editarTarea(tareaId: String, titulo: String, descripcion: String, fecha: String, prioridad: String) {
        viewModelScope.launch {
            tareasRef().document(tareaId).update(
                mapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "fecha" to fecha,
                    "prioridad" to prioridad
                )
            ).await()
            // Cancelar notificación anterior y reprogramar
            cancelarNotificacion(tareaId)
            programarNotificacion(tareaId, titulo, descripcion, fecha)
        }
    }

    /** Marca o desmarca una tarea como completada */
    fun toggleTarea(tareaId: String, completada: Boolean) {
        viewModelScope.launch {
            tareasRef().document(tareaId).update("completada", completada).await()
            // Si se completa, cancelar la notificación
            if (completada) cancelarNotificacion(tareaId)
        }
    }

    /** Elimina una tarea y cancela su notificación */
    fun eliminarTarea(tareaId: String) {
        viewModelScope.launch {
            tareasRef().document(tareaId).delete().await()
            cancelarNotificacion(tareaId)
        }
    }

    /** Programa una notificación para el día de la tarea a las 9:00 AM */
    private fun programarNotificacion(tareaId: String, titulo: String, descripcion: String, fecha: String) {
        if (fecha.isBlank()) return

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaTarea = runCatching { sdf.parse(fecha) }.getOrNull() ?: return

        // Programar para las 9:00 AM del día de la tarea
        val calendar = Calendar.getInstance().apply {
            time = fechaTarea
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        //val delay = 10_000L -> linea de codigo para probar las notificaciones en 10 seg
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        if (delay <= 0) return // Si la fecha ya pasó no programar

        val inputData = workDataOf(
            TareaNotificationWorker.KEY_TITULO to titulo,
            TareaNotificationWorker.KEY_DESCRIPCION to descripcion
        )

        val workRequest = OneTimeWorkRequestBuilder<TareaNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(tareaId) // Tag para poder cancelarla después
            .build()

        workManager.enqueueUniqueWork(
            tareaId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /** Cancela la notificación de una tarea */
    private fun cancelarNotificacion(tareaId: String) {
        workManager.cancelUniqueWork(tareaId)
    }
}