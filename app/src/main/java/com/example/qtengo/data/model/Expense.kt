package com.example.qtengo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un movimiento económico (Gasto o Ingreso).
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,          // Nombre del movimiento (Ej: Compra folios)
    val details: String = "",  // Descripción pequeña o notas adicionales
    val amount: Double,        // Cantidad de dinero
    val category: String,      // Categoría (Ventas, Suministros, etc.)
    val date: String,          // Fecha en formato dd/MM/yyyy
    val type: String,          // "GASTO" | "INGRESO"
    val profile: String        // FAMILIA | PYME
)
