package com.example.qtengo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para gestionar tareas o pendientes de la Pyme.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,         // Título de la tarea
    val description: String,   // Detalles
    val isCompleted: Boolean = false, // Estado
    val priority: String,      // ALTA | MEDIA | BAJA
    val profile: String        // PYME
)
