package com.example.nutriprontuario.ui.plans

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutriprontuario.databinding.ItemMealPlanBinding
import com.example.nutriprontuario.ui.plans.items.PlanItemsAdapter

data class MealUi(
    var title: String,
    val items: MutableList<String> = mutableListOf(),
    var observations: String? = null
)

class PlanMealsAdapter(
    private val meals: List<MealUi>,
    private val listener: MealListener
) : RecyclerView.Adapter<PlanMealsAdapter.MealViewHolder>() {

    interface MealListener {
        fun onUpdate(mealIndex: Int, title: String? = null, observations: String? = null)
        fun onAddItem(mealIndex: Int)
        fun onRemoveItem(mealIndex: Int, itemIndex: Int)
        fun onItemChanged(mealIndex: Int, itemIndex: Int, text: String)
        fun onRemoveMeal(mealIndex: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    class MealViewHolder(
        private val binding: ItemMealPlanBinding,
        private val listener: MealListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var itemsAdapter: PlanItemsAdapter
        private var titleWatcher: TextWatcher? = null

        private val obsWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val idx = bindingAdapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    listener.onUpdate(idx, observations = s?.toString())
                }
            }
        }

        fun bind(meal: MealUi) {
            titleWatcher?.let { binding.etMealTitle.removeTextChangedListener(it) }
            binding.etMealTitle.setText(meal.title)
            titleWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val idx = bindingAdapterPosition
                    if (idx != RecyclerView.NO_POSITION) {
                        listener.onUpdate(idx, title = s?.toString())
                    }
                }
            }
            binding.etMealTitle.addTextChangedListener(titleWatcher)

            binding.etObservations.removeTextChangedListener(obsWatcher)
            binding.etObservations.setText(meal.observations)
            binding.etObservations.addTextChangedListener(obsWatcher)

            itemsAdapter = PlanItemsAdapter(
                items = meal.items,
                onItemChanged = { idx, text ->
                    val mealIdx = bindingAdapterPosition
                    if (mealIdx != RecyclerView.NO_POSITION) {
                        listener.onItemChanged(mealIdx, idx, text)
                    }
                },
                onRemoveItem = { idx ->
                    val mealIdx = bindingAdapterPosition
                    if (mealIdx != RecyclerView.NO_POSITION) {
                        listener.onRemoveItem(mealIdx, idx)
                    }
                }
            )
            binding.rvItems.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvItems.adapter = itemsAdapter

            binding.btnAddItem.setOnClickListener {
                val idx = bindingAdapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    listener.onAddItem(idx)
                }
            }

            binding.btnRemoveMeal.setOnClickListener {
                val idx = bindingAdapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    listener.onRemoveMeal(idx)
                }
            }
        }
    }
}
