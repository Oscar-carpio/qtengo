package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val apellido1: String,
    val apellido2: String,
    val email: String,
    val password: String,
    val perfil: String
)