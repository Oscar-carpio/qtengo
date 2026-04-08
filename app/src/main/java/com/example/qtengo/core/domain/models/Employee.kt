package com.example.qtengo.core.domain.models

data class Employee(
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val salary: Double = 0.0,
    val phone: String = "",
    val startDate: String = "",
    val profile: String = ""
)