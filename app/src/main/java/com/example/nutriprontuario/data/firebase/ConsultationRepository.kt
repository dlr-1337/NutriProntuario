package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Consultation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class ConsultationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenConsultations(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<Consultation>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(PATIENTS_COLLECTION)
            .document(patientId.toString())
            .collection(COLLECTION)
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Consultation>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    fun saveConsultation(
        consultation: Consultation,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(consultation.patientId.toString())
            .collection(COLLECTION)
            .document()

        val data = consultation.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    companion object {
        private const val PATIENTS_COLLECTION = "patients"
        private const val COLLECTION = "consultations"
    }

    fun deleteConsultation(
        patientId: Long,
        consultationId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firestore.collection(PATIENTS_COLLECTION)
            .document(patientId.toString())
            .collection(COLLECTION)
            .document(consultationId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }
}
