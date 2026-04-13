package com.example.qtengo.core.domain.models

data class Product(
    val id: String = "",
    val customId: String = "", // Formato 001L
    val name: String = "",
    val quantity: Double = 0.0,
    val minStock: Double = 0.0,
    val category: String = "",
    val profile: String = "",  // FAMILIA | HOSTELERIA | PYME
    val unit: String = "",     // unidades, kg, litros...
    val notes: String = ""
)
