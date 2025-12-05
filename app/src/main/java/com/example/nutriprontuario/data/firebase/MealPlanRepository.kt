package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.MealPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

/**
 * Repositório responsável pelo gerenciamento de Planos Alimentares no Firebase Firestore.
 *
 * Esta classe implementa as operações CRUD para planos alimentares dos pacientes,
 * que contêm as refeições prescritas pelo nutricionista.
 * Estrutura: patients/{patientId}/plans/{planId}
 *
 * @property firestore Instância do Firebase Firestore para acesso ao banco de dados
 */
class MealPlanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Escuta alterações na lista de planos alimentares de um paciente em tempo real.
     *
     * Configura um listener no Firestore que é acionado sempre que há mudanças
     * nos planos do paciente. Os planos são ordenados por data (mais recentes primeiro).
     *
     * @param patientId ID do paciente cujos planos serão escutados
     * @param ownerUid UID do usuário proprietário (para filtro de segurança)
     * @param onUpdate Callback chamado quando a lista é atualizada
     * @param onError Callback chamado em caso de erro
     * @return ListenerRegistration para cancelar a escuta quando necessário
     */
    fun listenPlans(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<MealPlan>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(PATIENTS_COLLECTION)
            .document(patientId.toString())
            .collection(COLLECTION)
            .whereEqualTo("ownerUid", ownerUid) // Filtro de segurança por proprietário
            .addSnapshotListener { snapshot, error ->
                // Verifica se ocorreu erro na escuta
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                // Converte documentos para objetos MealPlan e ordena por data (decrescente)
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<MealPlan>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    /**
     * Salva um novo plano alimentar no Firestore.
     *
     * O ID do plano é gerado automaticamente pelo Firestore.
     * O plano é salvo como documento na subcoleção do paciente.
     *
     * @param plan Objeto MealPlan a ser salvo (com lista de refeições)
     * @param onComplete Callback com resultado (sucesso/falha) e mensagem de erro se houver
     */
    fun savePlan(
        plan: MealPlan,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Cria referência para novo documento (ID automático)
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(plan.patientId.toString())
            .collection(COLLECTION)
            .document()

        // Atualiza o objeto com o ID gerado e salva
        val data = plan.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    /**
     * Exclui um plano alimentar específico.
     *
     * @param patientId ID do paciente ao qual o plano pertence
     * @param planId ID do plano a ser excluído
     * @param onComplete Callback com resultado da operação
     */
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

    companion object {
        // Nome da coleção principal de pacientes
        private const val PATIENTS_COLLECTION = "patients"
        // Nome da subcoleção de planos alimentares
        private const val COLLECTION = "plans"
    }
}
