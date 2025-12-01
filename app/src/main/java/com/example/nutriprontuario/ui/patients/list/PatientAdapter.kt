package com.example.nutriprontuario.ui.patients.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.databinding.ItemPatientBinding

data class Patient(
    val id: Long,
    val name: String,
    val phone: String?,
    val lastAppointment: String?
)

class PatientAdapter(
    private val onItemClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PatientViewHolder(
        private val binding: ItemPatientBinding,
        private val onItemClick: (Patient) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Patient) {
            binding.tvName.text = patient.name
            binding.tvPhone.text = patient.phone ?: ""
            binding.tvLastAppointment.text = patient.lastAppointment ?: "Nunca"

            binding.root.setOnClickListener {
                onItemClick(patient)
            }
        }
    }

    private class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
