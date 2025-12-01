package com.example.nutriprontuario.ui.measurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.MeasurementRepository
import com.example.nutriprontuario.data.model.Measurement
import kotlin.math.pow

class MeasurementFormViewModel(
    private val repository: MeasurementRepository = MeasurementRepository()
) : ViewModel() {

    data class FormState(
        val imc: String = "--",
        val classification: String = "--",
        val isLoading: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false
    )

    private val _state = MutableLiveData(FormState())
    val state: LiveData<FormState> = _state

    fun calculateImc(weight: Double?, heightCm: Double?) {
        if (weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
            _state.value = _state.value?.copy(imc = "--", classification = "--")
            return
        }
        val heightM = heightCm / 100.0
        val imcValue = weight / heightM.pow(2)
        _state.value = _state.value?.copy(
            imc = "%.2f".format(imcValue),
            classification = getClassification(imcValue)
        )
    }

    fun saveMeasurement(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        weight: Double?,
        heightCm: Double?,
        waistCm: Double?
    ) {
        if (dateMillis == 0L || weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
            _state.value = _state.value?.copy(error = "Preencha data, peso e altura.")
            return
        }
        val imcValue = weight / (heightCm / 100.0).pow(2)
        val measurement = Measurement(
            patientId = patientId,
            date = dateMillis,
            weight = weight,
            heightCm = heightCm,
            waistCm = waistCm ?: 0.0,
            imc = imcValue,
            imcClassification = getClassification(imcValue),
            ownerUid = ownerUid
        )

        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)
        repository.saveMeasurement(measurement) { success, error ->
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

    private fun getClassification(imc: Double): String {
        return when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            imc < 35.0 -> "Obesidade grau I"
            imc < 40.0 -> "Obesidade grau II"
            else -> "Obesidade grau III"
        }
    }
}
