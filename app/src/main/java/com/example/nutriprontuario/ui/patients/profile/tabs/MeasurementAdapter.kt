package com.example.nutriprontuario.ui.patients.profile.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.data.model.Measurement
import com.example.nutriprontuario.databinding.ItemMeasurementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter responsável por exibir a lista de medidas antropométricas de um paciente.
 *
 * Cada item mostra a data, peso e IMC com classificação. Usa ListAdapter com DiffUtil
 * para atualizações eficientes quando novos dados são recebidos do Firestore.
 *
 * @property onClick Callback chamado ao clicar em uma medida para ver detalhes
 */
class MeasurementAdapter(
    private val onClick: (Measurement) -> Unit
) : ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder>(DiffCallback()) {

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MeasurementViewHolder(binding)
    }

    // Vincula os dados de uma medida a um ViewHolder existente
    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * ViewHolder responsável por exibir os dados de uma medida antropométrica individual.
     */
    class MeasurementViewHolder(
        private val binding: ItemMeasurementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula os dados da medida aos elementos visuais do item.
         *
         * Exibe data, peso formatado e IMC com classificação (ex: "Normal", "Sobrepeso").
         */
        fun bind(measurement: Measurement, onClick: (Measurement) -> Unit) {
            binding.tvDate.text = formatDate(measurement.date)
            // Exibe peso com uma casa decimal
            binding.tvWeight.text = "Peso: %.1f kg".format(measurement.weight)
            // Exibe IMC com duas casas decimais e a classificação
            binding.tvImc.text = "IMC: %.2f (%s)".format(
                measurement.imc,
                measurement.imcClassification
            )
            binding.root.setOnClickListener { onClick(measurement) }
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
     * Callback usado pelo DiffUtil para calcular diferenças entre listas de medidas.
     */
    private class DiffCallback : DiffUtil.ItemCallback<Measurement>() {
        // Verifica se dois itens representam a mesma medida (compara IDs)
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem.id == newItem.id
        }

        // Verifica se o conteúdo de duas medidas é idêntico
        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem == newItem
        }
    }
}
