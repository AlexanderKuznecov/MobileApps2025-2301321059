package com.example.healthyhabits.model

data class Habit(
    val id: Long,
    val name: String,
    val description: String? = null,
    val isCompleted: Boolean = false
)
