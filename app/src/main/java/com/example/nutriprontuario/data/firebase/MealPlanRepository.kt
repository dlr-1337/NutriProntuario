package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.MealPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class MealPlanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenPlans(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<MealPlan>) -> Unit,
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
                    doc.toObject<MealPlan>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    fun savePlan(
        plan: MealPlan,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(plan.patientId.toString())
            .collection(COLLECTION)
            .document()

        val data = plan.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    companion object {
        private const val PATIENTS_COLLECTION = "patients"
        private const val COLLECTION = "plans"
    }

    fun deletePlan(
        patientId: Long,
        planId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firestore.collection(PATIENTS_COLLECTION)
            .document(patientId.toString())
            .collection(COLLECTION)
            .document(planId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }
}
