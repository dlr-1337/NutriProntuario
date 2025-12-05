package com.example.nutriprontuario.data.model

/**
 * Modelo de dados que representa uma Consulta nutricional.
 *
 * Esta classe armazena as informações de uma consulta realizada com um paciente,
 * incluindo queixa principal, recordatório alimentar de 24h e evolução clínica.
 * Os dados são persistidos como subcoleção do paciente no Firebase Firestore.
 *
 * @property id Identificador único da consulta (gerado pelo Firestore)
 * @property patientId ID do paciente associado a esta consulta
 * @property date Data e hora da consulta em timestamp (milissegundos)
 * @property mainComplaint Motivo principal da consulta relatado pelo paciente
 * @property recall24h Descrição da alimentação do paciente nas últimas 24 horas
 * @property evolution Anotações sobre a evolução do tratamento nutricional
 * @property ownerUid UID do usuário Firebase proprietário deste registro
 */
data class Consultation(
    val id: String = "",                     // ID único da consulta (Firestore)
    val patientId: Long = -1,                // ID do paciente vinculado
    val date: Long = 0L,                     // Timestamp da data da consulta
    val mainComplaint: String = "",          // Queixa principal do paciente
    val recall24h: String = "",              // Recordatório alimentar de 24 horas
    val evolution: String = "",              // Evolução do tratamento
    val ownerUid: String = ""                // UID do nutricionista proprietário
)
