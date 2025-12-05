package com.example.nutriprontuario.ui.consultations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.ConsultationRepository
import com.example.nutriprontuario.data.model.Consultation

/**
 * ViewModel responsável pelo formulário de registro de consultas.
 *
 * Gerencia a validação e salvamento de novas consultas nutricionais,
 * incluindo queixa principal, recordatório 24h e evolução do tratamento.
 *
 * @property repository Repositório de consultas para acesso ao Firestore
 */
class ConsultationFormViewModel(
    private val repository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    /**
     * Classe de dados que representa o estado atual do formulário.
     *
     * @property isLoading Indica se uma operação está em andamento
     * @property error Mensagem de erro a ser exibida
     * @property saved Indica se a consulta foi salva com sucesso
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
     * Salva uma nova consulta no Firestore.
     *
     * Valida se a data foi preenchida antes de salvar.
     * Os demais campos (queixa, recordatório, evolução) são opcionais.
     *
     * @param patientId ID do paciente associado à consulta
     * @param ownerUid UID do usuário proprietário
     * @param dateMillis Data da consulta em timestamp (milissegundos)
     * @param mainComplaint Queixa principal do paciente
     * @param recall24h Recordatório alimentar de 24 horas
     * @param evolution Evolução do tratamento nutricional
     */
    fun saveConsultation(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        mainComplaint: String,
        recall24h: String,
        evolution: String
    ) {
        // Valida campo obrigatório
        if (dateMillis == 0L) {
            _state.value = _state.value?.copy(error = "Data é obrigatória.")
            return
        }

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)

        // Cria objeto Consultation com os dados informados
        val consultation = Consultation(
            patientId = patientId,
            date = dateMillis,
            mainComplaint = mainComplaint,
            recall24h = recall24h,
            evolution = evolution,
            ownerUid = ownerUid
        )

        // Salva no Firestore
        repository.saveConsultation(consultation) { success, error ->
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
