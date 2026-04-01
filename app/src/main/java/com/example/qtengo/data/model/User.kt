package com.example.qtengo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val nombre: String = "",
    val apellido1: String = "",
    val apellido2: String = "",
    val email: String = "",
    val perfil: String = ""
)