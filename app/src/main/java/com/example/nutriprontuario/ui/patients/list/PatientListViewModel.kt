package com.example.nutriprontuario.ui.patients.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.PatientRepository
import com.example.nutriprontuario.data.model.Patient
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel responsável pela lista de pacientes.
 *
 * Gerencia o carregamento, filtragem e exclusão de pacientes.
 * Mantém um listener em tempo real com o Firestore para
 * atualizações automáticas da lista.
 *
 * @property repository Repositório de pacientes para acesso ao Firestore
 */
class PatientListViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    // Listener do Firestore para atualizações em tempo real
    private var listener: ListenerRegistration? = null
    // Termo de busca atual para filtrar pacientes
    private var query: String = ""
    // UID do usuário autenticado (necessário para exclusão)
    private var ownerUid: String? = null

    // Lista completa de pacientes (sem filtro)
    private val _patients = MutableLiveData<List<Patient>>(emptyList())
    val patients: LiveData<List<Patient>> = _patients

    // Lista filtrada de pacientes (baseada na busca)
    private val _filtered = MutableLiveData<List<Patient>>(emptyList())
    val filtered: LiveData<List<Patient>> = _filtered

    // Estado de carregamento para exibir progress na UI
    val loading = MutableLiveData(false)
    // Mensagem de erro (se houver)
    val error = MutableLiveData<String?>(null)

    /**
     * Exclui um paciente e todos os seus dados relacionados (cascata).
     *
     * Remove o paciente junto com suas consultas, medidas e planos alimentares.
     *
     * @param patientId ID do paciente a ser excluído
     * @param onComplete Callback com resultado da operação
     */
    fun deletePatient(patientId: Long, onComplete: (Boolean, String?) -> Unit) {
        val uid = ownerUid
        if (uid.isNullOrEmpty()) {
            onComplete(false, "Usuário não autenticado.")
            return
        }
        repository.deletePatientCascade(patientId, uid, onComplete)
    }

    /**
     * Inicia a escuta de pacientes do usuário logado.
     *
     * Configura um listener em tempo real que atualiza a lista
     * automaticamente quando há mudanças no Firestore.
     * Se já existe um listener ativo, não cria outro.
     *
     * @param ownerUid UID do usuário proprietário dos pacientes
     */
    fun start(ownerUid: String) {
        // Evita criar múltiplos listeners
        if (listener != null) return

        this.ownerUid = ownerUid

        loading.value = true
        listener = repository.listenPatients(
            ownerUid = ownerUid,
            onUpdate = { list ->
                // Atualiza lista e remove estado de carregamento
                loading.postValue(false)
                updatePatients(list)
            },
            onError = { exception ->
                // Exibe erro e remove estado de carregamento
                loading.postValue(false)
                error.postValue(exception.localizedMessage)
            }
        )
    }

    /**
     * Define o termo de busca para filtrar pacientes.
     *
     * Filtra por nome ou telefone do paciente.
     *
     * @param newQuery Novo termo de busca
     */
    fun setQuery(newQuery: String) {
        query = newQuery
        applyFilter()
    }

    /**
     * Atualiza a lista completa de pacientes e aplica o filtro atual.
     *
     * @param list Nova lista de pacientes do Firestore
     */
    private fun updatePatients(list: List<Patient>) {
        _patients.postValue(list)
        // Aplica filtro se houver termo de busca
        val filtered = if (query.isBlank()) {
            list
        } else {
            val q = query.lowercase()
            list.filter {
                // Filtra por nome ou telefone
                it.name.lowercase().contains(q) ||
                    (it.phone?.lowercase()?.contains(q) == true)
            }
        }
        _filtered.postValue(filtered)
    }

    /**
     * Aplica o filtro de busca na lista atual de pacientes.
     *
     * Chamado quando o termo de busca muda sem atualização do Firestore.
     */
    private fun applyFilter() {
        val base = _patients.value.orEmpty()
        // Se não há termo de busca, mostra todos
        if (query.isBlank()) {
            _filtered.postValue(base)
            return
        }
        // Filtra por nome ou telefone (case-insensitive)
        val q = query.lowercase()
        _filtered.postValue(
            base.filter {
                it.name.lowercase().contains(q) ||
                    (it.phone?.lowercase()?.contains(q) == true)
            }
        )
    }

    /**
     * Chamado quando o ViewModel é destruído.
     *
     * Remove o listener do Firestore para evitar vazamento de memória
     * e callbacks após a destruição da tela.
     */
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
