package com.example.nutriprontuario.data.model

/**
 * Modelo de dados que representa um Paciente no sistema.
 *
 * Esta classe é usada para armazenar as informações básicas de um paciente
 * cadastrado pelo nutricionista. Os dados são persistidos no Firebase Firestore.
 *
 * @property id Identificador único do paciente (gerado automaticamente)
 * @property name Nome completo do paciente
 * @property phone Número de telefone para contato (opcional)
 * @property lastAppointment Data da última consulta realizada (opcional, formato String)
 * @property notes Anotações gerais sobre o paciente (opcional)
 * @property ownerUid UID do usuário Firebase proprietário deste registro
 */
data class Patient(
    val id: Long = -1,                      // ID único do paciente (-1 indica não salvo)
    val name: String = "",                   // Nome completo do paciente
    val phone: String? = null,               // Telefone de contato (opcional)
    val lastAppointment: String? = null,     // Data da última consulta (opcional)
    val notes: String? = null,               // Observações gerais (opcional)
    val ownerUid: String = ""                // UID do nutricionista proprietário
)
