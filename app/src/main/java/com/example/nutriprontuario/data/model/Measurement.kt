package com.example.nutriprontuario.data.model

data class Measurement(
    val id: String = "",
    val patientId: Long = -1,
    val date: Long = 0L,
    val weight: Double = 0.0,
    val heightCm: Double = 0.0,
    val waistCm: Double = 0.0,
    val imc: Double = 0.0,
    val imcClassification: String = "",
    val ownerUid: String = ""
)
