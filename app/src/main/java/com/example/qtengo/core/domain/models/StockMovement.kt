package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantityChanged: Double,
    val newQuantity: Double,
    val date: String,
    val profile: String
)