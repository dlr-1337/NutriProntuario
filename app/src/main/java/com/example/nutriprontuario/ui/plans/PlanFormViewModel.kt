package com.example.nutriprontuario.ui.plans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.MealPlanRepository
import com.example.nutriprontuario.data.model.MealEntry
import com.example.nutriprontuario.data.model.MealPlan

/**
 * ViewModel responsável pelo formulário de planos alimentares.
 *
 * Gerencia a validação e salvamento de planos alimentares completos,
 * incluindo todas as refeições (café da manhã, almoço, jantar, etc.)
 * com seus respectivos itens.
 *
 * @property repository Repositório de planos alimentares para acesso ao Firestore
 */
class PlanFormViewModel(
    private val repository: MealPlanRepository = MealPlanRepository()
) : ViewModel() {

    /**
     * Classe de dados que representa o estado atual do formulário.
     *
     * @property isLoading Indica se uma operação está em andamento
     * @property error Mensagem de erro a ser exibida
     * @property saved Indica se o plano foi salvo com sucesso
     */
    data class FormState(
        val isLoading: Boolean = false,   // Carregando (mostra progress)
        val error: String? = null,        // Mensagem de erro
        val saved: Boolean = false        // Salvo com sucesso
    )

    // Estado interno mutável do formulário
    private val _state = MutableLiveData(FormState())
    // Estado público imutável para observação pela UI
    val state: LiveData<FormState> = _state

    /**
     * Salva um novo plano alimentar no Firestore.
     *
     * Valida se a data foi preenchida antes de salvar.
     * O plano inclui uma lista de refeições (MealEntry) com nome,
     * itens e observações de cada refeição.
     *
     * @param patientId ID do paciente associado ao plano
     * @param ownerUid UID do usuário proprietário
     * @param dateMillis Data do plano em timestamp (milissegundos)
     * @param meals Lista de refeições que compõem o plano alimentar
     */
    fun savePlan(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        meals: List<MealEntry>
    ) {
        // Valida campo obrigatório
        if (dateMillis == 0L) {
            _state.value = _state.value?.copy(error = "Data é obrigatória.")
            return
        }

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)

        // Cria objeto MealPlan com os dados informados
        val plan = MealPlan(
            patientId = patientId,
            date = dateMillis,
            meals = meals,
            ownerUid = ownerUid
        )

        // Salva no Firestore
        repository.savePlan(plan) { success, error ->
            if (success) {
                // Salvo com sucesso
                _state.postValue(FormState(saved = true))
            } else {
                // Erro ao salvar
                _state.postValue(
                    _state.value?.copy(
                        isLoading = false,
                        error = error ?: "Algo deu errado. Tente novamente."
                    )
                )
            }
        }
    }
}
