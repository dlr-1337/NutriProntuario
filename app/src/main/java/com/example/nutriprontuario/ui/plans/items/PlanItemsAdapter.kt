package com.example.nutriprontuario.ui.plans.items

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriprontuario.databinding.ItemMealPlanEntryBinding

/**
 * Adapter responsável por exibir e gerenciar os itens individuais de uma refeição.
 *
 * Cada item representa um alimento ou observação dentro de uma refeição específica.
 * Este adapter é usado de forma aninhada dentro do PlanMealsAdapter.
 *
 * @property items Lista mutável de strings representando os itens da refeição
 * @property onItemChanged Callback chamado quando o texto de um item é alterado
 * @property onRemoveItem Callback chamado quando o usuário remove um item
 */
class PlanItemsAdapter(
    private val items: MutableList<String>,
    private val onItemChanged: (Int, String) -> Unit,
    private val onRemoveItem: (Int) -> Unit
) : RecyclerView.Adapter<PlanItemsAdapter.ItemViewHolder>() {

    // Cria uma nova instância de ViewHolder quando o RecyclerView precisa de um novo item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemMealPlanEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, onItemChanged, onRemoveItem)
    }

    // Vincula os dados de um item a um ViewHolder existente
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder responsável por exibir e gerenciar um item individual de refeição.
     *
     * Contém um campo de texto editável para o item e um botão para removê-lo.
     */
    class ItemViewHolder(
        private val binding: ItemMealPlanEntryBinding,
        private val onItemChanged: (Int, String) -> Unit,
        private val onRemoveItem: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

        /**
         * Vincula o texto do item ao campo editável e configura os listeners.
         *
         * @param text Texto atual do item
         * @param index Posição do item na lista
         */
        fun bind(text: String, index: Int) {
            // Remove o watcher anterior para evitar notificações duplicadas ao atualizar o texto
            watcher?.let { binding.etItem.removeTextChangedListener(it) }
            binding.etItem.setText(text)

            // Cria e adiciona um novo watcher que notifica mudanças no texto
            watcher = binding.etItem.createWatcher(index, onItemChanged)
            binding.etItem.addTextChangedListener(watcher)

            // Botão para remover este item da refeição
            binding.btnRemoveItem.setOnClickListener {
                onRemoveItem(index)
            }
        }

        /**
         * Função de extensão que cria um TextWatcher para um EditText.
         *
         * O watcher notifica o callback toda vez que o texto é alterado.
         *
         * @param index Índice do item na lista
         * @param callback Função a ser chamada quando o texto muda
         * @return TextWatcher configurado
         */
        private fun EditText.createWatcher(index: Int, callback: (Int, String) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Notifica o callback com o índice e o novo texto
                    callback(index, s?.toString().orEmpty())
                }
            }
        }
    }
}
