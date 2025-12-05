package com.example.nutriprontuario.ui.plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.MealEntry
import com.example.nutriprontuario.databinding.FragmentPlanFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment para criação e edição de planos alimentares.
 *
 * Permite criar um plano alimentar completo com:
 * - Data do plano
 * - Múltiplas refeições (café da manhã, almoço, jantar, etc.)
 * - Itens alimentares para cada refeição
 * - Observações específicas por refeição
 *
 * O usuário pode adicionar/remover refeições dinamicamente e adicionar/remover
 * itens em cada refeição. Os dados são salvos no Firestore na subcoleção 'plans'.
 */
class PlanFormFragment : Fragment(), PlanMealsAdapter.MealListener {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentPlanFormBinding? = null
    private val binding get() = _binding!!

    // Arguments recebidos via Safe Args (contém patientId)
    private val args: PlanFormFragmentArgs by navArgs()
    private val viewModel: PlanFormViewModel by viewModels()
    private lateinit var mealAdapter: PlanMealsAdapter

    // Lista de refeições com valores padrão (6 refeições comuns)
    private val meals = mutableListOf(
        MealUi("Cafe da Manha"),
        MealUi("Lanche da Manha"),
        MealUi("Almoco"),
        MealUi("Lanche da Tarde"),
        MealUi("Jantar"),
        MealUi("Ceia")
    )

    // Data selecionada para o plano (padrão: hoje)
    private var selectedDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.new_plan)
        binding.btnSave.setOnClickListener { savePlan() }
        setupDatePicker()
        setupMealList()
        binding.etDate.setText(formatDate(selectedDateMillis))

        observeViewModel()
    }

    /**
     * Configura o seletor de data usando MaterialDatePicker.
     * Ao clicar no campo de data, exibe um calendário para seleção.
     */
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.plan_date))
                .setSelection(selectedDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateMillis = selection
                binding.etDate.setText(formatDate(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    /**
     * Configura o RecyclerView de refeições e o botão para adicionar novas refeições.
     * Cada refeição é editável e pode conter múltiplos itens alimentares.
     */
    private fun setupMealList() {
        mealAdapter = PlanMealsAdapter(meals, this)
        binding.rvMeals.adapter = mealAdapter
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())

        // Botão para adicionar nova refeição
        binding.btnAddMeal.setOnClickListener {
            meals.add(MealUi("Nova refeicao"))
            mealAdapter.notifyItemInserted(meals.lastIndex)
            binding.rvMeals.smoothScrollToPosition(meals.lastIndex)
        }
    }

    /**
     * Observa o estado do ViewModel para reagir a erros e sucesso no salvamento.
     * Exibe mensagens via Snackbar e navega de volta em caso de sucesso.
     */
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            state.error?.let {
                Snackbar.make(
                    binding.root,
                    it.ifBlank { getString(R.string.error_generic) },
                    Snackbar.LENGTH_LONG
                ).show()
            }
            if (state.saved) {
                Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Valida e salva o plano alimentar no Firestore.
     * Converte a lista de refeições (MealUi) para o modelo de dados (MealEntry).
     * Verifica se o usuário está autenticado antes de salvar.
     */
    private fun savePlan() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // Usuário não autenticado - redireciona para login
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        // Converte as refeições da UI para o modelo de dados
        val mealEntries = meals.map { meal ->
            MealEntry(
                name = meal.title,
                items = meal.items.joinToString("\n") { it },
                observations = meal.observations.orEmpty()
            )
        }

        // Salva o plano com os dados do formulário
        viewModel.savePlan(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            dateMillis = selectedDateMillis,
            meals = mealEntries
        )
    }

    /**
     * Formata um timestamp em milissegundos para o formato dd/MM/yyyy.
     */
    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Callbacks do PlanMealsAdapter.MealListener para gerenciar as refeições

    /**
     * Chamado quando o título ou observações de uma refeição são atualizados.
     */
    override fun onUpdate(mealIndex: Int, title: String?, observations: String?) {
        title?.let { meals[mealIndex].title = it }
        observations?.let { meals[mealIndex].observations = it }
    }

    /**
     * Chamado quando um novo item alimentar é adicionado a uma refeição.
     */
    override fun onAddItem(mealIndex: Int) {
        meals[mealIndex].items.add("")
        mealAdapter.notifyItemChanged(mealIndex)
    }

    /**
     * Chamado quando um item alimentar é removido de uma refeição.
     */
    override fun onRemoveItem(mealIndex: Int, itemIndex: Int) {
        if (itemIndex in meals[mealIndex].items.indices) {
            meals[mealIndex].items.removeAt(itemIndex)
            mealAdapter.notifyItemChanged(mealIndex)
        }
    }

    /**
     * Chamado quando o texto de um item alimentar é alterado.
     */
    override fun onItemChanged(mealIndex: Int, itemIndex: Int, text: String) {
        if (itemIndex in meals[mealIndex].items.indices) {
            meals[mealIndex].items[itemIndex] = text
        }
    }

    /**
     * Chamado quando uma refeição completa é removida do plano.
     */
    override fun onRemoveMeal(mealIndex: Int) {
        if (mealIndex in meals.indices) {
            meals.removeAt(mealIndex)
            mealAdapter.notifyDataSetChanged()
        }
    }
}
