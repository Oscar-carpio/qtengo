package com.example.qtengo.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantityChanged: Double, // Positivo para añadir, negativo para quitar
    val newQuantity: Double,
    val date: String, // dd/MM/yyyy
    val profile: String
)
