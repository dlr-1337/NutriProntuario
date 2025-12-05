package com.example.nutriprontuario.ui.patients.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.ConsultationRepository
import com.example.nutriprontuario.data.firebase.MealPlanRepository
import com.example.nutriprontuario.data.firebase.MeasurementRepository
import com.example.nutriprontuario.data.firebase.PatientRepository
import com.example.nutriprontuario.data.model.Consultation
import com.example.nutriprontuario.data.model.MealPlan
import com.example.nutriprontuario.data.model.Measurement
import com.example.nutriprontuario.data.model.Patient
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel responsável pelos detalhes e perfil completo de um paciente.
 *
 * Gerencia o carregamento do paciente e suas subcoleções (consultas, medidas
 * e planos alimentares) com listeners em tempo real. Também fornece métodos
 * para exclusão de registros individuais.
 *
 * @property patientRepository Repositório de pacientes
 * @property consultationRepository Repositório de consultas
 * @property measurementRepository Repositório de medidas
 * @property mealPlanRepository Repositório de planos alimentares
 */
class PatientDetailViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository(),
    private val measurementRepository: MeasurementRepository = MeasurementRepository(),
    private val mealPlanRepository: MealPlanRepository = MealPlanRepository()
) : ViewModel() {

    // ID do paciente atual
    private var patientId: Long = -1
    // UID do usuário proprietário
    private var ownerUid: String = ""

    // Listeners do Firestore para atualizações em tempo real
    private var consultationListener: ListenerRegistration? = null
    private var measurementListener: ListenerRegistration? = null
    private var planListener: ListenerRegistration? = null

    // Dados do paciente
    private val _patient = MutableLiveData<Patient?>()
    val patient: LiveData<Patient?> = _patient

    // Lista de consultas do paciente
    private val _consultations = MutableLiveData<List<Consultation>>(emptyList())
    val consultations: LiveData<List<Consultation>> = _consultations

    // Lista de medidas antropométricas do paciente
    private val _measurements = MutableLiveData<List<Measurement>>(emptyList())
    val measurements: LiveData<List<Measurement>> = _measurements

    // Lista de planos alimentares do paciente
    private val _plans = MutableLiveData<List<MealPlan>>(emptyList())
    val plans: LiveData<List<MealPlan>> = _plans

    // Mensagem de erro (se houver)
    val error = MutableLiveData<String?>(null)

    /**
     * Inicia o carregamento de todos os dados do paciente.
     *
     * Carrega as informações básicas do paciente e configura listeners
     * em tempo real para consultas, medidas e planos alimentares.
     *
     * @param patientId ID do paciente a ser carregado
     * @param ownerUid UID do usuário atual para validação
     */
    fun start(patientId: Long, ownerUid: String) {
        this.patientId = patientId
        this.ownerUid = ownerUid
        loadPatient()           // Carrega dados básicos do paciente
        listenConsultations()   // Inicia listener de consultas
        listenMeasurements()    // Inicia listener de medidas
        listenPlans()           // Inicia listener de planos
    }

    /**
     * Carrega os dados básicos do paciente.
     *
     * Busca o paciente no Firestore e valida se pertence ao usuário atual.
     */
    private fun loadPatient() {
        patientRepository.getPatient(
            patientId = patientId,
            onResult = { fetched ->
                // Valida propriedade e atualiza estado
                if (fetched?.ownerUid == ownerUid) {
                    _patient.postValue(fetched)
                } else {
                    error.postValue("Paciente não encontrado.")
                }
            },
            onError = { exception ->
                error.postValue(exception.localizedMessage)
            }
        )
    }

    /**
     * Configura listener em tempo real para consultas do paciente.
     *
     * Remove listener anterior (se existir) antes de criar um novo.
     */
    private fun listenConsultations() {
        consultationListener?.remove() // Remove listener anterior
        consultationListener = consultationRepository.listenConsultations(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _consultations.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    /**
     * Configura listener em tempo real para medidas do paciente.
     *
     * Remove listener anterior (se existir) antes de criar um novo.
     */
    private fun listenMeasurements() {
        measurementListener?.remove() // Remove listener anterior
        measurementListener = measurementRepository.listenMeasurements(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _measurements.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    /**
     * Configura listener em tempo real para planos alimentares do paciente.
     *
     * Remove listener anterior (se existir) antes de criar um novo.
     */
    private fun listenPlans() {
        planListener?.remove() // Remove listener anterior
        planListener = mealPlanRepository.listenPlans(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _plans.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    /**
     * Exclui o paciente atual (sem cascata de subcoleções).
     *
     * @param onComplete Callback com resultado da operação
     */
    fun deletePatient(onComplete: (Boolean, String?) -> Unit) {
        patientRepository.deletePatient(patientId) { success, error ->
            onComplete(success, error)
        }
    }

    /**
     * Exclui uma consulta específica do paciente.
     *
     * @param id ID da consulta a ser excluída
     * @param onComplete Callback com resultado da operação
     */
    fun deleteConsultation(id: String, onComplete: (Boolean, String?) -> Unit) {
        consultationRepository.deleteConsultation(patientId, id, onComplete)
    }

    /**
     * Exclui uma medida específica do paciente.
     *
     * @param id ID da medida a ser excluída
     * @param onComplete Callback com resultado da operação
     */
    fun deleteMeasurement(id: String, onComplete: (Boolean, String?) -> Unit) {
        measurementRepository.deleteMeasurement(patientId, id, onComplete)
    }

    /**
     * Exclui um plano alimentar específico do paciente.
     *
     * @param id ID do plano a ser excluído
     * @param onComplete Callback com resultado da operação
     */
    fun deletePlan(id: String, onComplete: (Boolean, String?) -> Unit) {
        mealPlanRepository.deletePlan(patientId, id, onComplete)
    }

    /**
     * Chamado quando o ViewModel é destruído.
     *
     * Remove todos os listeners do Firestore para evitar vazamento de memória.
     */
    override fun onCleared() {
        consultationListener?.remove()
        measurementListener?.remove()
        planListener?.remove()
        super.onCleared()
    }
}
