package com.example.nutriprontuario.ui.plans

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.databinding.ItemMealSectionBinding

data class MealSection(
    val name: String,
    var items: String = "",
    var observations: String = ""
)

class MealSectionAdapter(
    private val meals: List<MealSection>
) : RecyclerView.Adapter<MealSectionAdapter.MealSectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealSectionViewHolder {
        val binding = ItemMealSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealSectionViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    class MealSectionViewHolder(
        private val binding: ItemMealSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: MealSection) {
            binding.tvMealName.text = meal.name
            binding.etItems.setText(meal.items)
            binding.etObservations.setText(meal.observations)
        }

        fun getData(): MealSection {
            return MealSection(
                name = binding.tvMealName.text.toString(),
                items = binding.etItems.text.toString(),
                observations = binding.etObservations.text.toString()
            )
        }
    }

    fun getAllMealData(): List<MealSection> {
        // This would need to be implemented properly with ViewHolder tracking
        return meals
    }
}
