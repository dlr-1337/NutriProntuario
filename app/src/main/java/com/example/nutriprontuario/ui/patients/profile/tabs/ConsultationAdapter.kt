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

class ConsultationAdapter(
    private val onClick: (Consultation) -> Unit
) : ListAdapter<Consultation, ConsultationAdapter.ConsultationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultationViewHolder {
        val binding = ItemConsultationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConsultationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ConsultationViewHolder(
        private val binding: ItemConsultationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(consultation: Consultation, onClick: (Consultation) -> Unit) {
            binding.tvDate.text = formatDate(consultation.date)
            binding.tvComplaint.text = consultation.mainComplaint.ifBlank { consultation.recall24h }
            binding.root.setOnClickListener { onClick(consultation) }
        }

        private fun formatDate(millis: Long): String {
            if (millis == 0L) return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Consultation>() {
        override fun areItemsTheSame(oldItem: Consultation, newItem: Consultation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Consultation, newItem: Consultation): Boolean {
            return oldItem == newItem
        }
    }
}
