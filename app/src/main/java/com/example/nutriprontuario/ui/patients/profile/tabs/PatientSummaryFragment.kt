package com.example.nutriprontuario.ui.patients.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.nutriprontuario.databinding.FragmentPatientSummaryBinding
import com.example.nutriprontuario.ui.patients.profile.PatientDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientSummaryFragment : Fragment() {

    private var _binding: FragmentPatientSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDetailViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.patient.observe(viewLifecycleOwner) { patient ->
            patient?.let {
                binding.tvName.text = it.name
                binding.tvPhone.text = "Telefone: ${it.phone.orEmpty()}"
                binding.tvNotes.text = "Observacoes: ${it.notes ?: "Sem observacoes"}"
            }
        }

        viewModel.measurements.observe(viewLifecycleOwner) { list ->
            val latest = list.firstOrNull()
            if (latest != null) {
                binding.tvLastWeight.text = "Ultimo peso: %.1f kg".format(latest.weight)
                binding.tvLastImc.text = "Ultimo IMC: %.2f (${latest.imcClassification})".format(latest.imc)
            } else {
                binding.tvLastWeight.text = "Ultimo peso: --"
                binding.tvLastImc.text = "Ultimo IMC: --"
            }
        }

        viewModel.consultations.observe(viewLifecycleOwner) { list ->
            val latestDate = list.firstOrNull()?.date
            binding.tvRecentData.text = latestDate?.let { "Ultima consulta: ${formatDate(it)}" } ?: "Ultima consulta: --"
        }
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

        fun newInstance(patientId: Long) = PatientSummaryFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
