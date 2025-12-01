package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class PatientRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenPatients(
        ownerUid: String,
        onUpdate: (List<Patient>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(COLLECTION)
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val patients = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Patient>()?.copy(id = doc.getLong("id") ?: -1)
                }?.sortedBy { it.name.lowercase() }.orEmpty()
                onUpdate(patients)
            }
    }

    fun getPatient(
        patientId: Long,
        onResult: (Patient?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection(COLLECTION)
            .document(patientId.toString())
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.toObject<Patient>())
            }
            .addOnFailureListener(onError)
    }

    fun savePatient(
        patient: Patient,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docId = if (patient.id != -1L) patient.id else System.currentTimeMillis()
        val data = patient.copy(id = docId)
        firestore.collection(COLLECTION)
            .document(docId.toString())
            .set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { exception ->
                onComplete(false, exception.localizedMessage)
            }
    }

    fun deletePatient(
        patientId: Long,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firestore.collection(COLLECTION)
            .document(patientId.toString())
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    companion object {
        private const val COLLECTION = "patients"
    }
}
