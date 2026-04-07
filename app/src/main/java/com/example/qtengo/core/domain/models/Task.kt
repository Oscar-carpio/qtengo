package com.example.qtengo.core.domain.models

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIA",
    val profile: String = "",
    val date: String = "" // Formato dd/MM/yyyy
)