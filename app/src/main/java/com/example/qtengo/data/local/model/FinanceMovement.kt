package com.example.qtengo.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_movements")
data class FinanceMovement(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val concept: String,

    val amount: Double,

    val type: String, // "INGRESO" o "GASTO"

    val date: String,

    val profile: String // "PYME"
)