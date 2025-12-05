package com.example.nutriprontuario.data.model

/**
 * Modelo de dados que representa uma Medida Antropométrica do paciente.
 *
 * Esta classe armazena as medidas corporais coletadas durante o acompanhamento
 * nutricional, incluindo peso, altura, circunferência da cintura e cálculo do IMC.
 * Os dados são persistidos como subcoleção do paciente no Firebase Firestore.
 *
 * @property id Identificador único da medida (gerado pelo Firestore)
 * @property patientId ID do paciente associado a esta medida
 * @property date Data e hora da medição em timestamp (milissegundos)
 * @property weight Peso do paciente em quilogramas (kg)
 * @property heightCm Altura do paciente em centímetros (cm)
 * @property waistCm Circunferência da cintura em centímetros (cm)
 * @property imc Índice de Massa Corporal calculado (peso/altura²)
 * @property imcClassification Classificação do IMC (ex: "Normal", "Sobrepeso", "Obesidade")
 * @property ownerUid UID do usuário Firebase proprietário deste registro
 */
data class Measurement(
    val id: String = "",                     // ID único da medida (Firestore)
    val patientId: Long = -1,                // ID do paciente vinculado
    val date: Long = 0L,                     // Timestamp da data da medição
    val weight: Double = 0.0,                // Peso em kg
    val heightCm: Double = 0.0,              // Altura em centímetros
    val waistCm: Double = 0.0,               // Circunferência da cintura em cm
    val imc: Double = 0.0,                   // IMC calculado
    val imcClassification: String = "",      // Classificação do IMC
    val ownerUid: String = ""                // UID do nutricionista proprietário
)
