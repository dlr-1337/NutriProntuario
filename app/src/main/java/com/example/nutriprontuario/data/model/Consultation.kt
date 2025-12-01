package com.example.nutriprontuario.data.model

data class Consultation(
    val id: String = "",
    val patientId: Long = -1,
    val date: Long = 0L,
    val mainComplaint: String = "",
    val recall24h: String = "",
    val evolution: String = "",
    val ownerUid: String = ""
)
