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

class PatientAdapter(
    private val onItemClick: (Patient) -> Unit,
    private val onEdit: (Patient) -> Unit,
    private val onDelete: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding, onItemClick, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PatientViewHolder(
        private val binding: ItemPatientBinding,
        private val onItemClick: (Patient) -> Unit,
        private val onEdit: (Patient) -> Unit,
        private val onDelete: (Patient) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Patient) {
            binding.tvName.text = patient.name
            binding.tvPhone.text = patient.phone ?: ""
            binding.tvLastAppointment.text = patient.lastAppointment ?: "Nunca"

            binding.root.setOnClickListener {
                onItemClick(patient)
            }

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

    private class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
