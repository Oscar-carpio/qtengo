package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val contactName: String,
    val phone: String,
    val email: String,
    val category: String,
    val profile: String
)