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
import com.example.nutriprontuario.databinding.FragmentPatientMeasurementsBinding
import com.example.nutriprontuario.ui.patients.profile.PatientDetailViewModel
import com.example.nutriprontuario.ui.patients.profile.PatientProfileFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment que exibe a lista de medições antropométricas de um paciente.
 *
 * Apresenta todas as medições registradas (peso, altura, cintura, IMC) em formato de cards,
 * permite visualizar detalhes de cada medição em um diálogo e oferece a opção de excluir medições.
 * Também permite adicionar novas medições através de um FAB.
 * Faz parte do ViewPager2 na tela de perfil do paciente.
 */
class PatientMeasurementsFragment : Fragment() {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentPatientMeasurementsBinding? = null
    private val binding get() = _binding!!

    private var patientId: Long = -1

    // ViewModel compartilhado com o fragment pai (PatientProfileFragment)
    private val viewModel: PatientDetailViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: MeasurementAdapter

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
        _binding = FragmentPatientMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    /**
     * Configura o RecyclerView com o adapter de medições.
     * O click em um item abre um diálogo com os detalhes da medição.
     */
    private fun setupRecyclerView() {
        adapter = MeasurementAdapter { measurement ->
            showDetailsDialog(measurement)
        }
        binding.rvMeasurements.adapter = adapter
    }

    /**
     * Configura o botão FAB para adicionar nova medição.
     * Navega para a tela de formulário de medição.
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToMeasurement(patientId)
            findNavController().navigate(action)
        }
    }

    /**
     * Observa a lista de medições no ViewModel.
     * Atualiza o adapter e exibe/oculta mensagem de lista vazia.
     */
    private fun observeViewModel() {
        viewModel.measurements.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }

    /**
     * Exibe um diálogo com os detalhes completos da medição.
     * Mostra peso, altura, cintura, IMC calculado e classificação.
     * Oferece opção de excluir a medição.
     */
    private fun showDetailsDialog(measurement: com.example.nutriprontuario.data.model.Measurement) {
        val message = buildString {
            append("Data: ${formatDate(measurement.date)}\n\n")
            append("Peso: %.1f kg\nAltura: %.1f cm\nCintura: %.1f cm".format(measurement.weight, measurement.heightCm, measurement.waistCm))
            append("\n\nIMC: %.2f (%s)".format(measurement.imc, measurement.imcClassification))
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detalhes da medida")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.delete) { _, _ ->
                // Exclui a medição e exibe feedback ao usuário
                viewModel.deleteMeasurement(measurement.id) { success, error ->
                    val text = if (success) "Medida apagada" else error ?: getString(R.string.error_generic)
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
        fun newInstance(patientId: Long) = PatientMeasurementsFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
