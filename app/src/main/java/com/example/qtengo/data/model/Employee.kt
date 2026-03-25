package com.example.qtengo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un empleado en la base de datos.
 */
@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,          // Nombre completo
    val position: String,      // Cargo o puesto (Administrativo, Operario, etc.)
    val salary: Double,        // Salario mensual
    val phone: String,         // Teléfono de contacto
    val startDate: String,     // Fecha de contratación
    val profile: String        // Perfil (PYME | HOSTELERIA)
)
