package com.example.nutriprontuario.ui.patients.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.Patient
import com.example.nutriprontuario.databinding.FragmentPatientFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PatientFormFragment : Fragment() {

    private var _binding: FragmentPatientFormBinding? = null
    private val binding get() = _binding!!

    private val args: PatientFormFragmentArgs by navArgs()
    private val viewModel: PatientFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitleAndBack()
        binding.btnSave.setOnClickListener { savePatient() }
        observeViewModel()

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        viewModel.loadPatient(args.patientId, currentUser.uid)
    }

    private fun setupTitleAndBack() {
        binding.tvTitle.text = getString(
            if (args.patientId == -1L) R.string.new_patient else R.string.edit_patient
        )
        binding.tvTitle.setOnClickListener { findNavController().navigateUp() }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.patient != null) {
                fillForm(state.patient)
            }
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

    private fun fillForm(patient: Patient) {
        if (binding.etName.text.isNullOrBlank()) {
            binding.etName.setText(patient.name)
        }
        if (binding.etPhone.text.isNullOrBlank()) {
            binding.etPhone.setText(patient.phone)
        }
        if (binding.etNotes.text.isNullOrBlank()) {
            binding.etNotes.setText(patient.notes)
        }
    }

    private fun savePatient() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim().ifEmpty { null }
        val notes = binding.etNotes.text.toString().trim().ifEmpty { null }

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            return
        }
        binding.tilName.error = null

        viewModel.savePatient(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            name = name,
            phone = phone,
            notes = notes
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
