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

class PatientDetailViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository(),
    private val measurementRepository: MeasurementRepository = MeasurementRepository(),
    private val mealPlanRepository: MealPlanRepository = MealPlanRepository()
) : ViewModel() {

    private var patientId: Long = -1
    private var ownerUid: String = ""

    private var consultationListener: ListenerRegistration? = null
    private var measurementListener: ListenerRegistration? = null
    private var planListener: ListenerRegistration? = null

    private val _patient = MutableLiveData<Patient?>()
    val patient: LiveData<Patient?> = _patient

    private val _consultations = MutableLiveData<List<Consultation>>(emptyList())
    val consultations: LiveData<List<Consultation>> = _consultations

    private val _measurements = MutableLiveData<List<Measurement>>(emptyList())
    val measurements: LiveData<List<Measurement>> = _measurements

    private val _plans = MutableLiveData<List<MealPlan>>(emptyList())
    val plans: LiveData<List<MealPlan>> = _plans

    val error = MutableLiveData<String?>(null)

    fun start(patientId: Long, ownerUid: String) {
        this.patientId = patientId
        this.ownerUid = ownerUid
        loadPatient()
        listenConsultations()
        listenMeasurements()
        listenPlans()
    }

    private fun loadPatient() {
        patientRepository.getPatient(
            patientId = patientId,
            onResult = { fetched ->
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

    private fun listenConsultations() {
        consultationListener?.remove()
        consultationListener = consultationRepository.listenConsultations(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _consultations.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    private fun listenMeasurements() {
        measurementListener?.remove()
        measurementListener = measurementRepository.listenMeasurements(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _measurements.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    private fun listenPlans() {
        planListener?.remove()
        planListener = mealPlanRepository.listenPlans(
            patientId = patientId,
            ownerUid = ownerUid,
            onUpdate = { list -> _plans.postValue(list) },
            onError = { exception -> error.postValue(exception.localizedMessage) }
        )
    }

    fun deletePatient(onComplete: (Boolean, String?) -> Unit) {
        patientRepository.deletePatient(patientId) { success, error ->
            onComplete(success, error)
        }
    }

    fun deleteConsultation(id: String, onComplete: (Boolean, String?) -> Unit) {
        consultationRepository.deleteConsultation(patientId, id, onComplete)
    }

    fun deleteMeasurement(id: String, onComplete: (Boolean, String?) -> Unit) {
        measurementRepository.deleteMeasurement(patientId, id, onComplete)
    }

    fun deletePlan(id: String, onComplete: (Boolean, String?) -> Unit) {
        mealPlanRepository.deletePlan(patientId, id, onComplete)
    }

    override fun onCleared() {
        consultationListener?.remove()
        measurementListener?.remove()
        planListener?.remove()
        super.onCleared()
    }
}
