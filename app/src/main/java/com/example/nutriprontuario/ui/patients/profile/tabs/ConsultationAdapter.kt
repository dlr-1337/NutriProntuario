package com.example.nutriprontuario.ui.patients.profile.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.data.model.Consultation
import com.example.nutriprontuario.databinding.ItemConsultationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter responsável por exibir a lista de consultas de um paciente.
 *
 * Cada item mostra a data da consulta e um resumo (queixa principal ou recordatório de 24h).
 * Usa ListAdapter com DiffUtil para atualizações eficientes da lista.
 *
 * @property onClick Callback chamado ao clicar em uma consulta para ver detalhes
 */
class ConsultationAdapter(
    private val onClick: (Consultation) -> Unit
) : ListAdapter<Consultation, ConsultationAdapter.ConsultationViewHolder>(DiffCallback()) {

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultationViewHolder {
        val binding = ItemConsultationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConsultationViewHolder(binding)
    }

    // Vincula os dados de uma consulta a um ViewHolder existente
    override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * ViewHolder responsável por exibir os dados de uma consulta individual.
     */
    class ConsultationViewHolder(
        private val binding: ItemConsultationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula os dados da consulta aos elementos visuais do item.
         *
         * Exibe a data formatada e um resumo (queixa principal ou recordatório).
         */
        fun bind(consultation: Consultation, onClick: (Consultation) -> Unit) {
            binding.tvDate.text = formatDate(consultation.date)
            // Exibe a queixa principal; se vazia, mostra o recordatório de 24h
            binding.tvComplaint.text = consultation.mainComplaint.ifBlank { consultation.recall24h }
            binding.root.setOnClickListener { onClick(consultation) }
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
     * Callback usado pelo DiffUtil para calcular diferenças entre listas de consultas.
     */
    private class DiffCallback : DiffUtil.ItemCallback<Consultation>() {
        // Verifica se dois itens representam a mesma consulta (compara IDs)
        override fun areItemsTheSame(oldItem: Consultation, newItem: Consultation): Boolean {
            return oldItem.id == newItem.id
        }

        // Verifica se o conteúdo de duas consultas é idêntico
        override fun areContentsTheSame(oldItem: Consultation, newItem: Consultation): Boolean {
            return oldItem == newItem
        }
    }
}
