package com.example.qtengo.core.domain.models

data class Expense(
    val id: String = "",
    val name: String = "",
    val details: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val type: String = "",
    val profile: String = ""
)