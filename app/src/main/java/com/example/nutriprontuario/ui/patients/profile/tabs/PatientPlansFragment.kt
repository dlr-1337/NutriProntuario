package com.example.nutriprontuario.ui.patients.profile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.databinding.FragmentPatientPlansBinding
import com.example.nutriprontuario.ui.patients.profile.PatientProfileFragmentDirections

class PatientPlansFragment : Fragment() {

    private var _binding: FragmentPatientPlansBinding? = null
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
        _binding = FragmentPatientPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFab()
        loadPlans()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = PatientProfileFragmentDirections
                .actionProfileToPlan(patientId)
            findNavController().navigate(action)
        }
    }

    private fun loadPlans() {
        // For demo, show empty state
        binding.tvEmpty.isVisible = true
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
