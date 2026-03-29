package com.example.qtengo.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: String,
    val profile: String,
    val date: String // Formato dd/MM/yyyy
)