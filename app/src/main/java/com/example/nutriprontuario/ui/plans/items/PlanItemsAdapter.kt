package com.example.nutriprontuario.ui.plans.items

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.databinding.ItemMealPlanEntryBinding

class PlanItemsAdapter(
    private val items: MutableList<String>,
    private val onItemChanged: (Int, String) -> Unit,
    private val onRemoveItem: (Int) -> Unit
) : RecyclerView.Adapter<PlanItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemMealPlanEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, onItemChanged, onRemoveItem)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(
        private val binding: ItemMealPlanEntryBinding,
        private val onItemChanged: (Int, String) -> Unit,
        private val onRemoveItem: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

        fun bind(text: String, index: Int) {
            watcher?.let { binding.etItem.removeTextChangedListener(it) }
            binding.etItem.setText(text)
            watcher = binding.etItem.createWatcher(index, onItemChanged)
            binding.etItem.addTextChangedListener(watcher)

            binding.btnRemoveItem.setOnClickListener {
                onRemoveItem(index)
            }
        }

        private fun EditText.createWatcher(index: Int, callback: (Int, String) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    callback(index, s?.toString().orEmpty())
                }
            }
        }
    }
}
