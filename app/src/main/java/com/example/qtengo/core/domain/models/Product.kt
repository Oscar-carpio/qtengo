package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Double,
    val minStock: Double,
    val category: String,
    val profile: String,  // FAMILIA | HOSTELERIA | PYME
    val unit: String,     // unidades, kg, litros...
    val notes: String = ""
)