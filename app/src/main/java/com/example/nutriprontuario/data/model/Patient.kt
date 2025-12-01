package com.example.nutriprontuario.data.model

data class Patient(
    val id: Long = -1,
    val name: String = "",
    val phone: String? = null,
    val lastAppointment: String? = null,
    val notes: String? = null,
    val ownerUid: String = ""
)
