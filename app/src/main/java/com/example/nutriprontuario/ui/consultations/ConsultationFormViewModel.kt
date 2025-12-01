package com.example.nutriprontuario.ui.consultations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.ConsultationRepository
import com.example.nutriprontuario.data.model.Consultation

class ConsultationFormViewModel(
    private val repository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    data class FormState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false
    )

    private val _state = MutableLiveData(FormState())
    val state: LiveData<FormState> = _state

    fun saveConsultation(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        mainComplaint: String,
        recall24h: String,
        evolution: String
    ) {
        if (dateMillis == 0L) {
            _state.value = _state.value?.copy(error = "Data é obrigatória.")
            return
        }
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)

        val consultation = Consultation(
            patientId = patientId,
            date = dateMillis,
            mainComplaint = mainComplaint,
            recall24h = recall24h,
            evolution = evolution,
            ownerUid = ownerUid
        )

        repository.saveConsultation(consultation) { success, error ->
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
