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

    fun updatePatientFields(
        patientId: Long,
        name: String,
        phone: String?,
        notes: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "notes" to notes
        )
        firestore.collection(COLLECTION)
            .document(patientId.toString())
            .update(updates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    fun deletePatientCascade(
        patientId: Long,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val patientDoc = firestore.collection(COLLECTION).document(patientId.toString())

        fun deleteCollection(path: String, onDone: (Boolean, String?) -> Unit) {
            patientDoc.collection(path).get()
                .addOnSuccessListener { snap ->
                    val tasks = snap.documents.map { it.reference.delete() }
                    if (tasks.isEmpty()) onDone(true, null) else {
                        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener { onDone(true, null) }
                            .addOnFailureListener { e -> onDone(false, e.localizedMessage) }
                    }
                }
                .addOnFailureListener { e -> onDone(false, e.localizedMessage) }
        }

        deleteCollection("consultations") { ok1, err1 ->
            if (!ok1) {
                onComplete(false, err1)
                return@deleteCollection
            }
            deleteCollection("measurements") { ok2, err2 ->
                if (!ok2) {
                    onComplete(false, err2)
                    return@deleteCollection
                }
                deleteCollection("plans") { ok3, err3 ->
                    if (!ok3) {
                        onComplete(false, err3)
                        return@deleteCollection
                    }
                    patientDoc.delete()
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
                }
            }
        }
    }

    companion object {
        private const val COLLECTION = "patients"
    }
}
