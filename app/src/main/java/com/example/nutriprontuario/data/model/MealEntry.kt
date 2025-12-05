package com.example.nutriprontuario.data.model

/**
 * Modelo de dados que representa uma Refeição dentro de um Plano Alimentar.
 *
 * Esta classe armazena as informações de uma refeição específica (café da manhã,
 * almoço, lanche, jantar, etc.) com seus respectivos itens alimentares e observações.
 * É utilizada como parte da lista de refeições em um MealPlan (Plano Alimentar).
 *
 * @property name Nome da refeição (ex: "Café da Manhã", "Almoço", "Lanche da Tarde")
 * @property items Lista de alimentos/itens da refeição em formato texto
 * @property observations Observações adicionais sobre a refeição (ex: horário, quantidade)
 */
data class MealEntry(
    val name: String = "",                   // Nome da refeição (Café, Almoço, etc.)
    val items: String = "",                  // Itens/alimentos da refeição
    val observations: String = ""            // Observações adicionais
)
