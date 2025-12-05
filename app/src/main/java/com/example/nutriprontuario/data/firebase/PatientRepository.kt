package com.example.nutriprontuario.data.firebase

import com.example.nutriprontuario.data.model.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

/**
 * Repositório responsável pelo gerenciamento de Pacientes no Firebase Firestore.
 *
 * Esta classe implementa as operações CRUD (Criar, Ler, Atualizar, Deletar) para
 * pacientes, incluindo escuta em tempo real para atualizações automáticas na UI.
 *
 * @property firestore Instância do Firebase Firestore para acesso ao banco de dados
 */
class PatientRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Escuta alterações na lista de pacientes em tempo real.
     *
     * Configura um listener no Firestore que é acionado sempre que há mudanças
     * na coleção de pacientes do usuário atual. Os pacientes são ordenados
     * alfabeticamente pelo nome.
     *
     * @param ownerUid UID do usuário proprietário dos pacientes
     * @param onUpdate Callback chamado quando a lista é atualizada
     * @param onError Callback chamado em caso de erro
     * @return ListenerRegistration para cancelar a escuta quando necessário
     */
    fun listenPatients(
        ownerUid: String,
        onUpdate: (List<Patient>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(COLLECTION)
            .whereEqualTo("ownerUid", ownerUid) // Filtra pacientes do usuário atual
            .addSnapshotListener { snapshot, error ->
                // Verifica se ocorreu erro na escuta
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                // Converte documentos para objetos Patient e ordena por nome
                val patients = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Patient>()?.copy(id = doc.getLong("id") ?: -1)
                }?.sortedBy { it.name.lowercase() }.orEmpty()
                onUpdate(patients)
            }
    }

    /**
     * Busca um paciente específico pelo ID.
     *
     * @param patientId ID do paciente a ser buscado
     * @param onResult Callback com o paciente encontrado ou null se não existir
     * @param onError Callback chamado em caso de erro
     */
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

    /**
     * Salva um novo paciente ou atualiza um existente.
     *
     * Se o paciente não possui ID (id == -1), gera um novo ID baseado no timestamp atual.
     * Caso contrário, atualiza o documento existente com o mesmo ID.
     *
     * @param patient Objeto Patient a ser salvo
     * @param onComplete Callback com resultado (sucesso/falha) e mensagem de erro se houver
     */
    fun savePatient(
        patient: Patient,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Gera novo ID se for um novo paciente, senão mantém o existente
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

    /**
     * Exclui um paciente pelo ID (sem exclusão em cascata).
     *
     * ATENÇÃO: Este método apenas exclui o documento do paciente.
     * Para excluir também subcoleções (consultas, medidas, planos),
     * use o método deletePatientCascade().
     *
     * @param patientId ID do paciente a ser excluído
     * @param onComplete Callback com resultado da operação
     */
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

    /**
     * Atualiza campos específicos de um paciente.
     *
     * Útil para atualizações parciais sem precisar reenviar todos os dados.
     *
     * @param patientId ID do paciente a ser atualizado
     * @param name Novo nome do paciente
     * @param phone Novo telefone (pode ser null)
     * @param notes Novas observações (pode ser null)
     * @param onComplete Callback com resultado da operação
     */
    fun updatePatientFields(
        patientId: Long,
        name: String,
        phone: String?,
        notes: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Mapa com os campos a serem atualizados
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

    /**
     * Exclui um paciente e todas as suas subcoleções (exclusão em cascata).
     *
     * Este método garante que todas as consultas, medidas e planos alimentares
     * associados ao paciente sejam excluídos antes de remover o documento principal.
     * A exclusão é feita sequencialmente: consultas → medidas → planos → paciente.
     *
     * @param patientId ID do paciente a ser excluído com todos os dados relacionados
     * @param onComplete Callback com resultado da operação completa
     */
    fun deletePatientCascade(
        patientId: Long,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val patientDoc = firestore.collection(COLLECTION).document(patientId.toString())

        /**
         * Função auxiliar para excluir todos os documentos de uma subcoleção.
         *
         * @param path Nome da subcoleção a ser excluída
         * @param onDone Callback chamado ao finalizar a exclusão
         */
        fun deleteCollection(path: String, onDone: (Boolean, String?) -> Unit) {
            patientDoc.collection(path).get()
                .addOnSuccessListener { snap ->
                    // Cria lista de tarefas de exclusão para cada documento
                    val tasks = snap.documents.map { it.reference.delete() }
                    if (tasks.isEmpty()) {
                        onDone(true, null) // Subcoleção vazia, continua
                    } else {
                        // Aguarda todas as exclusões completarem
                        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener { onDone(true, null) }
                            .addOnFailureListener { e -> onDone(false, e.localizedMessage) }
                    }
                }
                .addOnFailureListener { e -> onDone(false, e.localizedMessage) }
        }

        // Exclusão sequencial: consultas → medidas → planos → documento do paciente
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
                    // Todas as subcoleções excluídas, agora exclui o paciente
                    patientDoc.delete()
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
                }
            }
        }
    }

    companion object {
        // Nome da coleção principal de pacientes no Firestore
        private const val COLLECTION = "patients"
    }
}
