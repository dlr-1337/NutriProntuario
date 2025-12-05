package com.example.nutriprontuario.data.model

/**
 * Modelo de dados que representa um Plano Alimentar completo.
 *
 * Esta classe armazena um plano alimentar prescrito para um paciente,
 * contendo todas as refeições do dia com seus respectivos itens.
 * Os dados são persistidos como subcoleção do paciente no Firebase Firestore.
 *
 * @property id Identificador único do plano alimentar (gerado pelo Firestore)
 * @property patientId ID do paciente associado a este plano
 * @property date Data de criação do plano em timestamp (milissegundos)
 * @property meals Lista de refeições (MealEntry) que compõem o plano
 * @property notes Observações gerais sobre o plano alimentar (opcional)
 * @property ownerUid UID do usuário Firebase proprietário deste registro
 */
data class MealPlan(
    val id: String = "",                     // ID único do plano (Firestore)
    val patientId: Long = -1,                // ID do paciente vinculado
    val date: Long = 0L,                     // Timestamp da data de criação
    val meals: List<MealEntry> = emptyList(), // Lista de refeições do plano
    val notes: String? = null,               // Observações gerais (opcional)
    val ownerUid: String = ""                // UID do nutricionista proprietário
)
