package com.example.nutriprontuario.ui.patients.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutriprontuario.data.firebase.PatientRepository
import com.example.nutriprontuario.data.model.Patient
import com.google.firebase.firestore.ListenerRegistration

class PatientListViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    private var listener: ListenerRegistration? = null
    private var query: String = ""

    private val _patients = MutableLiveData<List<Patient>>(emptyList())
    val patients: LiveData<List<Patient>> = _patients

    private val _filtered = MutableLiveData<List<Patient>>(emptyList())
    val filtered: LiveData<List<Patient>> = _filtered

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun deletePatient(patientId: Long, onComplete: (Boolean, String?) -> Unit) {
        repository.deletePatientCascade(patientId, onComplete)
    }

    fun start(ownerUid: String) {
        if (listener != null) return
        loading.value = true
        listener = repository.listenPatients(
            ownerUid = ownerUid,
            onUpdate = { list ->
                loading.postValue(false)
                updatePatients(list)
            },
            onError = { exception ->
                loading.postValue(false)
                error.postValue(exception.localizedMessage)
            }
        )
    }

    fun setQuery(newQuery: String) {
        query = newQuery
        applyFilter()
    }

    private fun updatePatients(list: List<Patient>) {
        _patients.postValue(list)
        val filtered = if (query.isBlank()) {
            list
        } else {
            val q = query.lowercase()
            list.filter {
                it.name.lowercase().contains(q) ||
                    (it.phone?.lowercase()?.contains(q) == true)
            }
        }
        _filtered.postValue(filtered)
    }

    private fun applyFilter() {
        val base = _patients.value.orEmpty()
        if (query.isBlank()) {
            _filtered.postValue(base)
            return
        }
        val q = query.lowercase()
        _filtered.postValue(
            base.filter {
                it.name.lowercase().contains(q) ||
                    (it.phone?.lowercase()?.contains(q) == true)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
