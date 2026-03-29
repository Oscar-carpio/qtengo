package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val position: String,
    val salary: Double,
    val phone: String,
    val startDate: String,
    val profile: String
)