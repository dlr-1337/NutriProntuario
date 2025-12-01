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

class PatientPlansFragment : Fragment() {

    private var _binding: FragmentPatientPlansBinding? = null
    private val binding get() = _binding!!

    private var patientId: Long = -1
    private val viewModel: PatientDetailViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: PlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun setupRecyclerView() {
        adapter = PlanAdapter { plan ->
            showDetailsDialog(plan)
        }
        binding.rvPlans.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToPlan(patientId)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.plans.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }

    private fun showDetailsDialog(plan: com.example.nutriprontuario.data.model.MealPlan) {
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
                viewModel.deletePlan(plan.id) { success, error ->
                    val text = if (success) "Plano apagado" else error ?: getString(R.string.error_generic)
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
                }
            }
            .show()
    }

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

        fun newInstance(patientId: Long) = PatientPlansFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
