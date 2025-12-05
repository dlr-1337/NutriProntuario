package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Consultation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

/**
 * Repositório responsável pelo gerenciamento de Consultas no Firebase Firestore.
 *
 * Esta classe implementa as operações CRUD para consultas nutricionais,
 * que são armazenadas como subcoleção dentro do documento de cada paciente.
 * Estrutura: patients/{patientId}/consultations/{consultationId}
 *
 * @property firestore Instância do Firebase Firestore para acesso ao banco de dados
 */
class ConsultationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Escuta alterações na lista de consultas de um paciente em tempo real.
     *
     * Configura um listener no Firestore que é acionado sempre que há mudanças
     * nas consultas do paciente. As consultas são ordenadas por data (mais recentes primeiro).
     *
     * @param patientId ID do paciente cujas consultas serão escutadas
     * @param ownerUid UID do usuário proprietário (para filtro de segurança)
     * @param onUpdate Callback chamado quando a lista é atualizada
     * @param onError Callback chamado em caso de erro
     * @return ListenerRegistration para cancelar a escuta quando necessário
     */
    fun listenConsultations(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<Consultation>) -> Unit,
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
                // Converte documentos para objetos Consultation e ordena por data (decrescente)
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Consultation>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    /**
     * Salva uma nova consulta no Firestore.
     *
     * O ID da consulta é gerado automaticamente pelo Firestore.
     * A consulta é salva como documento na subcoleção do paciente.
     *
     * @param consultation Objeto Consultation a ser salvo
     * @param onComplete Callback com resultado (sucesso/falha) e mensagem de erro se houver
     */
    fun saveConsultation(
        consultation: Consultation,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Cria referência para novo documento (ID automático)
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(consultation.patientId.toString())
            .collection(COLLECTION)
            .document()

        // Atualiza o objeto com o ID gerado e salva
        val data = consultation.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    /**
     * Exclui uma consulta específica.
     *
     * @param patientId ID do paciente ao qual a consulta pertence
     * @param consultationId ID da consulta a ser excluída
     * @param onComplete Callback com resultado da operação
     */
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

    companion object {
        // Nome da coleção principal de pacientes
        private const val PATIENTS_COLLECTION = "patients"
        // Nome da subcoleção de consultas
        private const val COLLECTION = "consultations"
    }
}
