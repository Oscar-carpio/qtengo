package com.example.qtengo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un proveedor en la base de datos.
 */
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,          // Nombre de la empresa proveedora
    val contactName: String,   // Persona de contacto
    val phone: String,         // Teléfono
    val email: String,         // Correo electrónico
    val category: String,      // Tipo de suministros (Alimentación, Limpieza, etc.)
    val profile: String        // Perfil que lo usa (PYME | HOSTELERIA)
)
