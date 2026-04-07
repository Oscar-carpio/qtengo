package com.example.qtengo.core.domain.models

data class StockMovement(
    val id: String = "",
    val productId: Int = 0,
    val productName: String = "",
    val quantityChanged: Double = 0.0,
    val newQuantity: Double = 0.0,
    val date: String = "",
    val profile: String = ""
)