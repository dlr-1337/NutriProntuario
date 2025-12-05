package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Measurement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

/**
 * Repositório responsável pelo gerenciamento de Medidas Antropométricas no Firebase Firestore.
 *
 * Esta classe implementa as operações CRUD para medidas corporais dos pacientes,
 * incluindo peso, altura, circunferência da cintura e IMC.
 * Estrutura: patients/{patientId}/measurements/{measurementId}
 *
 * @property firestore Instância do Firebase Firestore para acesso ao banco de dados
 */
class MeasurementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Escuta alterações na lista de medidas de um paciente em tempo real.
     *
     * Configura um listener no Firestore que é acionado sempre que há mudanças
     * nas medidas do paciente. As medidas são ordenadas por data (mais recentes primeiro).
     *
     * @param patientId ID do paciente cujas medidas serão escutadas
     * @param ownerUid UID do usuário proprietário (para filtro de segurança)
     * @param onUpdate Callback chamado quando a lista é atualizada
     * @param onError Callback chamado em caso de erro
     * @return ListenerRegistration para cancelar a escuta quando necessário
     */
    fun listenMeasurements(
        patientId: Long,
        ownerUid: String,
        onUpdate: (List<Measurement>) -> Unit,
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
                // Converte documentos para objetos Measurement e ordena por data (decrescente)
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Measurement>()?.copy(id = doc.id)
                }?.sortedByDescending { it.date } ?: emptyList()
                onUpdate(items)
            }
    }

    /**
     * Salva uma nova medida antropométrica no Firestore.
     *
     * O ID da medida é gerado automaticamente pelo Firestore.
     * A medida é salva como documento na subcoleção do paciente.
     *
     * @param measurement Objeto Measurement a ser salvo (com peso, altura, IMC, etc.)
     * @param onComplete Callback com resultado (sucesso/falha) e mensagem de erro se houver
     */
    fun saveMeasurement(
        measurement: Measurement,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Cria referência para novo documento (ID automático)
        val docRef = firestore.collection(PATIENTS_COLLECTION)
            .document(measurement.patientId.toString())
            .collection(COLLECTION)
            .document()

        // Atualiza o objeto com o ID gerado e salva
        val data = measurement.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage) }
    }

    /**
     * Exclui uma medida específica.
     *
     * @param patientId ID do paciente ao qual a medida pertence
     * @param measurementId ID da medida a ser excluída
     * @param onComplete Callback com resultado da operação
     */
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

    companion object {
        // Nome da coleção principal de pacientes
        private const val PATIENTS_COLLECTION = "patients"
        // Nome da subcoleção de medidas
        private const val COLLECTION = "measurements"
    }
}
