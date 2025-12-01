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

class PlanAdapter(
    private val onClick: (MealPlan) -> Unit
) : ListAdapter<MealPlan, PlanAdapter.PlanViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class PlanViewHolder(
        private val binding: ItemPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: MealPlan, onClick: (MealPlan) -> Unit) {
            binding.tvDate.text = "Plano de ${formatDate(plan.date)}"
            binding.root.setOnClickListener { onClick(plan) }
        }

        private fun formatDate(millis: Long): String {
            if (millis == 0L) return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MealPlan>() {
        override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem == newItem
        }
    }
}
