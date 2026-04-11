package com.example.qtengo.familiar.ui.tareas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TareaNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "tareas_channel"
        const val KEY_TITULO = "titulo"
        const val KEY_DESCRIPCION = "descripcion"
        const val KEY_TAREA_ID = "tareaId"   // FIX WARN — nuevo campo para ID único
    }

    override fun doWork(): Result {
        val titulo = inputData.getString(KEY_TITULO) ?: "Tarea pendiente"
        val descripcion = inputData.getString(KEY_DESCRIPCION) ?: ""
        val tareaId = inputData.getString(KEY_TAREA_ID) ?: titulo

        crearCanalNotificacion()

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ $titulo")
            .setContentText(descripcion.ifBlank { "Tienes una tarea pendiente para hoy" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // FIX WARN — usamos el hash del tareaId en lugar de currentTimeMillis().toInt()
        // El tareaId es el ID del documento Firestore, único por definición
        manager.notify(tareaId.hashCode(), notificacion)

        return Result.success()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Tareas y recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de tareas pendientes"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }
}