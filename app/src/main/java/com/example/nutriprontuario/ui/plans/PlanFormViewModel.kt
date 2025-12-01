package com.example.nutriprontuario.ui.plans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.MealPlanRepository
import com.example.nutriprontuario.data.model.MealEntry
import com.example.nutriprontuario.data.model.MealPlan

class PlanFormViewModel(
    private val repository: MealPlanRepository = MealPlanRepository()
) : ViewModel() {

    data class FormState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val saved: Boolean = false
    )

    private val _state = MutableLiveData(FormState())
    val state: LiveData<FormState> = _state

    fun savePlan(
        patientId: Long,
        ownerUid: String,
        dateMillis: Long,
        meals: List<MealEntry>
    ) {
        if (dateMillis == 0L) {
            _state.value = _state.value?.copy(error = "Data é obrigatória.")
            return
        }
        _state.value = _state.value?.copy(isLoading = true, error = null, saved = false)
        val plan = MealPlan(
            patientId = patientId,
            date = dateMillis,
            meals = meals,
            ownerUid = ownerUid
        )
        repository.savePlan(plan) { success, error ->
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
