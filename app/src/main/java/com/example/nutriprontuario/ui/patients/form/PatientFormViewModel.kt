package com.example.nutriprontuario.ui.patients.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.PatientRepository
import com.example.nutriprontuario.data.model.Patient

class PatientFormViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    data class FormState(
        val patient: Patient? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false
    )

    private val _state = MutableLiveData(FormState())
    val state: LiveData<FormState> = _state

    fun loadPatient(patientId: Long, ownerUid: String) {
        if (patientId == -1L) return
        _state.value = _state.value?.copy(isLoading = true)
        repository.getPatient(
            patientId = patientId,
            onResult = { patient ->
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

    fun savePatient(
        patientId: Long,
        ownerUid: String,
        name: String,
        phone: String?,
        notes: String?
    ) {
        if (name.isBlank()) {
            _state.value = _state.value?.copy(error = "Nome é obrigatório.")
            return
        }
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)
        val patient = Patient(
            id = if (patientId == -1L) System.currentTimeMillis() else patientId,
            name = name,
            phone = phone,
            notes = notes,
            ownerUid = ownerUid
        )
        repository.savePatient(patient) { success, error ->
            if (success) {
                _state.postValue(FormState(saved = true))
            } else {
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
