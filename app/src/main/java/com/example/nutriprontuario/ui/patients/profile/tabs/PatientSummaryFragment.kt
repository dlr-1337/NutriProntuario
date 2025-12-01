package com.example.nutriprontuario.ui.patients.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutriprontuario.databinding.FragmentPatientSummaryBinding

class PatientSummaryFragment : Fragment() {

    private var _binding: FragmentPatientSummaryBinding? = null
    private val binding get() = _binding!!

    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patientId = arguments?.getLong(ARG_PATIENT_ID) ?: -1
    }

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
        loadPatientData()
    }

    private fun loadPatientData() {
        // Load patient data based on patientId
        binding.tvName.text = "Maria Silva"
        binding.tvPhone.text = "Telefone: (62) 98765-4321"
        binding.tvNotes.text = "Observações: Paciente com restrição alimentar"
        binding.tvLastWeight.text = "Último peso: 65.0 kg"
        binding.tvLastImc.text = "Último IMC: 23.5 (Peso normal)"
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
