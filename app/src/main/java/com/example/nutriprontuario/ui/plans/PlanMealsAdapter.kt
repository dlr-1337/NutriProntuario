package com.example.nutriprontuario.ui.plans

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutriprontuario.databinding.ItemMealPlanBinding
import com.example.nutriprontuario.ui.plans.items.PlanItemsAdapter

/**
 * Modelo de dados UI para representar uma refeição em um plano alimentar.
 *
 * @property title Título da refeição (ex: "Café da manhã", "Almoço")
 * @property items Lista de itens/alimentos da refeição
 * @property observations Observações opcionais sobre a refeição
 */
data class MealUi(
    var title: String,
    val items: MutableList<String> = mutableListOf(),
    var observations: String? = null
)

/**
 * Adapter responsável por exibir e gerenciar a lista de refeições em um plano alimentar.
 *
 * Este adapter permite adicionar/remover refeições e itens, além de editar títulos e
 * observações. Cada refeição contém um adapter aninhado (PlanItemsAdapter) para
 * gerenciar seus itens individuais.
 *
 * @property meals Lista de refeições do plano alimentar
 * @property listener Interface para callbacks de ações do usuário
 */
class PlanMealsAdapter(
    private val meals: List<MealUi>,
    private val listener: MealListener
) : RecyclerView.Adapter<PlanMealsAdapter.MealViewHolder>() {

    /**
     * Interface para comunicar ações do usuário ao Fragment/ViewModel.
     */
    interface MealListener {
        fun onUpdate(mealIndex: Int, title: String? = null, observations: String? = null)
        fun onAddItem(mealIndex: Int)
        fun onRemoveItem(mealIndex: Int, itemIndex: Int)
        fun onItemChanged(mealIndex: Int, itemIndex: Int, text: String)
        fun onRemoveMeal(mealIndex: Int)
    }

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding, listener)
    }

    // Vincula os dados de uma refeição a um ViewHolder existente
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    /**
     * ViewHolder responsável por exibir e gerenciar uma refeição individual.
     *
     * Contém campos editáveis para título e observações, além de um RecyclerView
     * aninhado para os itens da refeição.
     */
    class MealViewHolder(
        private val binding: ItemMealPlanBinding,
        private val listener: MealListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var itemsAdapter: PlanItemsAdapter
        private var titleWatcher: TextWatcher? = null

        // TextWatcher para observações - notifica o listener quando o texto muda
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

        /**
         * Vincula os dados da refeição aos elementos visuais do item.
         *
         * Configura TextWatchers para título e observações, inicializa o adapter
         * de itens aninhado e configura botões de ação.
         */
        fun bind(meal: MealUi) {
            // Remove o watcher anterior antes de atualizar o texto para evitar loops
            titleWatcher?.let { binding.etMealTitle.removeTextChangedListener(it) }
            binding.etMealTitle.setText(meal.title)

            // Cria e adiciona um novo watcher para o título
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

            // Configura watcher para observações
            binding.etObservations.removeTextChangedListener(obsWatcher)
            binding.etObservations.setText(meal.observations)
            binding.etObservations.addTextChangedListener(obsWatcher)

            // Configura o adapter aninhado para os itens desta refeição
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

            // Botão para adicionar novo item à refeição
            binding.btnAddItem.setOnClickListener {
                val idx = bindingAdapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    listener.onAddItem(idx)
                }
            }

            // Botão para remover a refeição inteira do plano
            binding.btnRemoveMeal.setOnClickListener {
                val idx = bindingAdapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    listener.onRemoveMeal(idx)
                }
            }
        }
    }
}
