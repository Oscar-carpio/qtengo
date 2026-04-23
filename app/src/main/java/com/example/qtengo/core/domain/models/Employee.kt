package com.example.qtengo.core.domain.models

data class Employee(
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val salary: Double = 0.0,
    val phone: String = "",
    val email: String = "",
    val startDate: String = "",
    val profile: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)