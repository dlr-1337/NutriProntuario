package com.example.nutriprontuario.ui.patients.profile.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.data.model.MealPlan
import com.example.nutriprontuario.databinding.ItemPlanBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter responsável por exibir a lista de planos alimentares de um paciente.
 *
 * Cada item mostra a data do plano alimentar. Usa ListAdapter com DiffUtil para
 * atualizações eficientes quando novos planos são recebidos do Firestore.
 *
 * @property onClick Callback chamado ao clicar em um plano para ver detalhes
 */
class PlanAdapter(
    private val onClick: (MealPlan) -> Unit
) : ListAdapter<MealPlan, PlanAdapter.PlanViewHolder>(DiffCallback()) {

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }

    // Vincula os dados de um plano a um ViewHolder existente
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * ViewHolder responsável por exibir os dados de um plano alimentar individual.
     */
    class PlanViewHolder(
        private val binding: ItemPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula os dados do plano aos elementos visuais do item.
         *
         * Exibe uma descrição do plano com a data formatada.
         */
        fun bind(plan: MealPlan, onClick: (MealPlan) -> Unit) {
            binding.tvDate.text = "Plano de ${formatDate(plan.date)}"
            binding.root.setOnClickListener { onClick(plan) }
        }

        /**
         * Formata um timestamp (milissegundos) para o formato dd/MM/yyyy.
         *
         * @param millis Timestamp em milissegundos
         * @return Data formatada ou "--" se o valor for 0
         */
        private fun formatDate(millis: Long): String {
            if (millis == 0L) return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }

    /**
     * Callback usado pelo DiffUtil para calcular diferenças entre listas de planos.
     */
    private class DiffCallback : DiffUtil.ItemCallback<MealPlan>() {
        // Verifica se dois itens representam o mesmo plano (compara IDs)
        override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem.id == newItem.id
        }

        // Verifica se o conteúdo de dois planos é idêntico
        override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem == newItem
        }
    }
}
