package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Measurement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class MeasurementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenMeasurements(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<Measurement>) -> Unit,
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
                    doc.toObject<Measurement>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    fun saveMeasurement(
        measurement: Measurement,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(measurement.patientId.toString())
            .collection(COLLECTION)
            .document()

        val data = measurement.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    companion object {
        private const val PATIENTS_COLLECTION = "patients"
        private const val COLLECTION = "measurements"
    }

    fun deleteMeasurement(
        patientId: Long,
        measurementId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firestore.collection(PATIENTS_COLLECTION)
            .document(patientId.toString())
            .collection(COLLECTION)
            .document(measurementId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }
}
