package com.example.qtengo.core.domain.models

data class Supplier(
    val id: String = "",
    val name: String = "",
    val contactName: String = "",
    val phone: String = "",
    val email: String = "",
    val category: String = "",
    val profile: String = "",
    val timestamp: Long = System.currentTimeMillis()
)