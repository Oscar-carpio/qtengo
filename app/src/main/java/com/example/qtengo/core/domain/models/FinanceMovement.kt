package com.example.qtengo.core.domain.models

data class FinanceMovement(
    val id: String = "",
    val concept: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "INGRESO" o "GASTO"
    val date: String = "",
    val profile: String = "", // "PYME"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)