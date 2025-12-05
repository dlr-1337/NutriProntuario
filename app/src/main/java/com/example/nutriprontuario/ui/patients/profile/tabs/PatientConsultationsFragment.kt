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
import com.example.nutriprontuario.databinding.FragmentPatientConsultationsBinding
import com.example.nutriprontuario.ui.patients.profile.PatientDetailViewModel
import com.example.nutriprontuario.ui.patients.profile.PatientProfileFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment que exibe a lista de consultas de um paciente.
 *
 * Apresenta todas as consultas registradas em formato de cards, permite visualizar
 * detalhes de cada consulta em um diálogo e oferece a opção de excluir consultas.
 * Também permite adicionar novas consultas através de um FAB.
 * Faz parte do ViewPager2 na tela de perfil do paciente.
 */
class PatientConsultationsFragment : Fragment() {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentPatientConsultationsBinding? = null
    private val binding get() = _binding!!

    private var patientId: Long = -1

    // ViewModel compartilhado com o fragment pai (PatientProfileFragment)
    private val viewModel: PatientDetailViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: ConsultationAdapter

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
        _binding = FragmentPatientConsultationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    /**
     * Configura o RecyclerView com o adapter de consultas.
     * O click em um item abre um diálogo com os detalhes da consulta.
     */
    private fun setupRecyclerView() {
        adapter = ConsultationAdapter { consultation ->
            showDetailsDialog(consultation)
        }
        binding.rvConsultations.adapter = adapter
    }

    /**
     * Configura o botão FAB para adicionar nova consulta.
     * Navega para a tela de formulário de consulta.
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToConsultation(patientId)
            findNavController().navigate(action)
        }
    }

    /**
     * Observa a lista de consultas no ViewModel.
     * Atualiza o adapter e exibe/oculta mensagem de lista vazia.
     */
    private fun observeViewModel() {
        viewModel.consultations.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }

    /**
     * Exibe um diálogo com os detalhes completos da consulta.
     * Permite visualizar queixa, recordatório 24h e evolução/conduta.
     * Oferece opção de excluir a consulta.
     */
    private fun showDetailsDialog(consultation: com.example.nutriprontuario.data.model.Consultation) {
        val message = buildString {
            append("Data: ${formatDate(consultation.date)}\n\n")
            if (consultation.mainComplaint.isNotBlank()) {
                append("Queixa: ${consultation.mainComplaint}\n\n")
            }
            if (consultation.recall24h.isNotBlank()) {
                append("Recordatorio: ${consultation.recall24h}\n\n")
            }
            if (consultation.evolution.isNotBlank()) {
            append("Evolucao/Conduta: ${consultation.evolution}")
            }
        }.trim()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detalhes da consulta")
            .setMessage(message.ifBlank { "Sem detalhes" })
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.delete) { _, _ ->
                // Exclui a consulta e exibe feedback ao usuário
                viewModel.deleteConsultation(consultation.id) { success, error ->
                    val text = if (success) "Consulta apagada" else error ?: getString(R.string.error_generic)
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
        fun newInstance(patientId: Long) = PatientConsultationsFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
