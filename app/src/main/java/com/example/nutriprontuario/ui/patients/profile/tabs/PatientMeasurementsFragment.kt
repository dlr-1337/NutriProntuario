package com.example.nutriprontuario.ui.patients.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.databinding.FragmentPatientMeasurementsBinding
import com.example.nutriprontuario.ui.patients.profile.PatientProfileFragmentDirections

class PatientMeasurementsFragment : Fragment() {

    private var _binding: FragmentPatientMeasurementsBinding? = null
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
        _binding = FragmentPatientMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFab()
        loadMeasurements()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToMeasurement(patientId)
            findNavController().navigate(action)
        }
    }

    private fun loadMeasurements() {
        // For demo, show empty state
        binding.tvEmpty.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PATIENT_ID = "patient_id"

        fun newInstance(patientId: Long) = PatientMeasurementsFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PATIENT_ID, patientId)
            }
        }
    }
}
