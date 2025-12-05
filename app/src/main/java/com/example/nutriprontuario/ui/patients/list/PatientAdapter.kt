package com.example.nutriprontuario.ui.patients.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.Patient
import com.example.nutriprontuario.databinding.ItemPatientBinding

/**
 * Adapter responsável por exibir a lista de pacientes em um RecyclerView.
 *
 * Este adapter usa ListAdapter com DiffUtil para cálculo eficiente de diferenças,
 * permitindo animações automáticas quando a lista é atualizada. Cada item exibe
 * informações do paciente e um menu de opções (editar/excluir).
 *
 * @property onItemClick Callback chamado ao clicar no card do paciente
 * @property onEdit Callback chamado ao selecionar "Editar" no menu
 * @property onDelete Callback chamado ao selecionar "Excluir" no menu
 */
class PatientAdapter(
    private val onItemClick: (Patient) -> Unit,
    private val onEdit: (Patient) -> Unit,
    private val onDelete: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding, onItemClick, onEdit, onDelete)
    }

    // Vincula os dados de um paciente a um ViewHolder existente
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder responsável por exibir os dados de um paciente individual.
     *
     * Configura os listeners para clique no card e no botão de menu (três pontos).
     */
    class PatientViewHolder(
        private val binding: ItemPatientBinding,
        private val onItemClick: (Patient) -> Unit,
        private val onEdit: (Patient) -> Unit,
        private val onDelete: (Patient) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula os dados do paciente aos elementos visuais do item.
         *
         * Exibe nome, telefone e última consulta, além de configurar o menu popup
         * com opções de editar e excluir.
         */
        fun bind(patient: Patient) {
            // Exibe os dados do paciente
            binding.tvName.text = patient.name
            binding.tvPhone.text = patient.phone ?: ""
            binding.tvLastAppointment.text = patient.lastAppointment ?: "Nunca"

            // Clique no card abre o perfil do paciente
            binding.root.setOnClickListener {
                onItemClick(patient)
            }

            // Clique no botão de três pontos exibe menu de opções
            binding.btnMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.menu_patient_item)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEdit(patient)
                            true
                        }
                        R.id.action_delete -> {
                            onDelete(patient)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    /**
     * Callback usado pelo DiffUtil para calcular diferenças entre listas de pacientes.
     *
     * Permite que o RecyclerView atualize eficientemente apenas os itens que mudaram,
     * com animações automáticas.
     */
    private class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        // Verifica se dois itens representam o mesmo paciente (compara IDs)
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        // Verifica se o conteúdo de dois pacientes é idêntico
        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
