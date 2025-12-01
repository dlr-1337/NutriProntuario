package com.example.nutriprontuario.data.model

data class MealPlan(
    val id: String = "",
    val patientId: Long = -1,
    val date: Long = 0L,
    val meals: List<MealEntry> = emptyList(),
    val notes: String? = null,
    val ownerUid: String = ""
)
