package com.example.qtengo.core.domain.models

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para las tareas.
 * Se utiliza @get:PropertyName para asegurar que Firebase use el nombre exacto de la propiedad
 * y evitar problemas con el prefijo 'is' en booleano.
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    
    val priority: String = "MEDIA",
    val profile: String = "",
    val date: String = "", // Fecha programada dd/MM/yyyy
    val createdAt: String = "", // Fecha de creación dd/MM/yyyy
    val timestamp: Long = System.currentTimeMillis()
)
