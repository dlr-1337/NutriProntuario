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

class MeasurementAdapter(
    private val onClick: (Measurement) -> Unit
) : ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MeasurementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class MeasurementViewHolder(
        private val binding: ItemMeasurementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(measurement: Measurement, onClick: (Measurement) -> Unit) {
            binding.tvDate.text = formatDate(measurement.date)
            binding.tvWeight.text = "Peso: %.1f kg".format(measurement.weight)
            binding.tvImc.text = "IMC: %.2f (%s)".format(
                measurement.imc,
                measurement.imcClassification
            )
            binding.root.setOnClickListener { onClick(measurement) }
        }

        private fun formatDate(millis: Long): String {
            if (millis == 0L) return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem == newItem
        }
    }
}
