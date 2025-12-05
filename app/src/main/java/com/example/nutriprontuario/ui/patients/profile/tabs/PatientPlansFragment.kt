package com.example.nutriprontuario.ui.patients.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPatientPlansBinding
import com.example.nutriprontuario.ui.patients.profile.PatientDetailViewModel
import com.example.nutriprontuario.ui.patients.profile.PatientProfileFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment que exibe a lista de planos alimentares de um paciente.
 *
 * Apresenta todos os planos alimentares registrados em formato de cards,
 * permite visualizar detalhes de cada plano (incluindo todas as refeições e itens) em um diálogo
 * e oferece a opção de excluir planos. Também permite adicionar novos planos através de um FAB.
 * Faz parte do ViewPager2 na tela de perfil do paciente.
 */
class PatientPlansFragment : Fragment() {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentPatientPlansBinding? = null
    private val binding get() = _binding!!

    private var patientId: Long = -1

    // ViewModel compartilhado com o fragment pai (PatientProfileFragment)
    private val viewModel: PatientDetailViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: PlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recupera o ID do paciente dos argumentos
        patientId = arguments?.getLong(ARG_PATIENT_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    /**
     * Configura o RecyclerView com o adapter de planos alimentares.
     * O click em um item abre um diálogo com os detalhes do plano.
     */
    private fun setupRecyclerView() {
        adapter = PlanAdapter { plan ->
            showDetailsDialog(plan)
        }
        binding.rvPlans.adapter = adapter
    }

    /**
     * Configura o botão FAB para adicionar novo plano alimentar.
     * Navega para a tela de formulário de plano.
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToPlan(patientId)
            findNavController().navigate(action)
        }
    }

    /**
     * Observa a lista de planos alimentares no ViewModel.
     * Atualiza o adapter e exibe/oculta mensagem de lista vazia.
     */
    private fun observeViewModel() {
        viewModel.plans.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }

    /**
     * Exibe um diálogo com os detalhes completos do plano alimentar.
     * Mostra todas as refeições, itens de cada refeição e observações.
     * Oferece opção de excluir o plano.
     */
    private fun showDetailsDialog(plan: com.example.nutriprontuario.data.model.MealPlan) {
        // Formata as refeições com seus itens
        val mealsText = plan.meals.joinToString("\n\n") { meal ->
            val items = meal.items.split("\n").joinToString("\n") { "- $it" }
            buildString {
                append("${meal.name}:\n")
                if (items.isNotBlank()) append(items)
                if (!meal.observations.isNullOrBlank()) {
                    append("\nObs: ${meal.observations}")
                }
            }
        }

        val message = buildString {
            append("Data: ${formatDate(plan.date)}\n\n")
            append(mealsText.ifBlank { "Sem itens" })
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detalhes do plano")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.delete) { _, _ ->
                // Exclui o plano e exibe feedback ao usuário
                viewModel.deletePlan(plan.id) { success, error ->
                    val text = if (success) "Plano apagado" else error ?: getString(R.string.error_generic)
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
                }
            }
            .show()
    }

    /**
     * Formata um timestamp em milissegundos para o formato dd/MM/yyyy.
     */
    private fun formatDate(millis: Long): String {
        if (millis == 0L) return "--"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PATIENT_ID = "patient_id"

        /**
         * Método factory para criar uma nova instância do fragment com o ID do paciente.
         */
        fun newInstance(patientId: Long) = PatientPlansFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
