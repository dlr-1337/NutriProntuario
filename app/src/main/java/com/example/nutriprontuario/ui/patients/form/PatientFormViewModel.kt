package com.example.nutriprontuario.ui.patients.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.PatientRepository
import com.example.nutriprontuario.data.model.Patient

/**
 * ViewModel responsável pelo formulário de cadastro/edição de pacientes.
 *
 * Gerencia o carregamento de dados existentes (modo edição) e o salvamento
 * de novos pacientes ou atualizações. Valida campos obrigatórios antes de salvar.
 *
 * @property repository Repositório de pacientes para acesso ao Firestore
 */
class PatientFormViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    /**
     * Classe de dados que representa o estado atual do formulário.
     *
     * @property patient Dados do paciente carregado (modo edição)
     * @property isLoading Indica se uma operação está em andamento
     * @property error Mensagem de erro a ser exibida
     * @property saved Indica se os dados foram salvos com sucesso
     */
    data class FormState(
        val patient: Patient? = null,     // Paciente carregado (edição)
        val isLoading: Boolean = false,   // Carregando (mostra progress)
        val error: String? = null,        // Mensagem de erro
        val saved: Boolean = false        // Salvo com sucesso
    )

    // Estado interno mutável do formulário
    private val _state = MutableLiveData(FormState())
    // Estado público imutável para observação pela UI
    val state: LiveData<FormState> = _state

    /**
     * Carrega os dados de um paciente existente para edição.
     *
     * Busca o paciente pelo ID e valida se pertence ao usuário atual.
     * Se o ID for -1, ignora (modo criação).
     *
     * @param patientId ID do paciente a ser carregado (-1 para novo)
     * @param ownerUid UID do usuário atual para validação
     */
    fun loadPatient(patientId: Long, ownerUid: String) {
        // Ignora se for um novo paciente
        if (patientId == -1L) return

        _state.value = _state.value?.copy(isLoading = true)
        repository.getPatient(
            patientId = patientId,
            onResult = { patient ->
                // Valida se o paciente pertence ao usuário atual
                val valid = patient?.ownerUid == ownerUid
                _state.postValue(
                    _state.value?.copy(
                        patient = if (valid) patient else null,
                        isLoading = false,
                        error = if (valid) null else "Paciente não encontrado."
                    )
                )
            },
            onError = { exception ->
                _state.postValue(
                    _state.value?.copy(
                        isLoading = false,
                        error = exception.localizedMessage
                    )
                )
            }
        )
    }

    /**
     * Salva um novo paciente ou atualiza um existente.
     *
     * Valida se o nome está preenchido antes de salvar.
     * Gera um novo ID baseado no timestamp se for um novo paciente.
     *
     * @param patientId ID do paciente (-1 para novo)
     * @param ownerUid UID do usuário proprietário
     * @param name Nome do paciente (obrigatório)
     * @param phone Telefone do paciente (opcional)
     * @param notes Observações sobre o paciente (opcional)
     */
    fun savePatient(
        patientId: Long,
        ownerUid: String,
        name: String,
        phone: String?,
        notes: String?
    ) {
        // Valida campo obrigatório
        if (name.isBlank()) {
            _state.value = _state.value?.copy(error = "Nome é obrigatório.")
            return
        }

        // Inicia estado de carregamento
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)

        // Cria objeto Patient com ID novo ou existente
        val patient = Patient(
            id = if (patientId == -1L) System.currentTimeMillis() else patientId,
            name = name,
            phone = phone,
            notes = notes,
            ownerUid = ownerUid
        )

        // Salva no Firestore
        repository.savePatient(patient) { success, error ->
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
