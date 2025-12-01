package com.example.nutriprontuario.ui.consultations

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
import com.example.nutriprontuario.databinding.FragmentConsultationFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultationFormFragment : Fragment() {

    private var _binding: FragmentConsultationFormBinding? = null
    private val binding get() = _binding!!

    private val args: ConsultationFormFragmentArgs by navArgs()
    private val viewModel: ConsultationFormViewModel by viewModels()

    private var selectedDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.evolution)
        binding.btnSave.setOnClickListener { saveConsultation() }

        setupDatePicker()
        binding.etDate.setText(formatDate(selectedDateMillis))

        observeViewModel()
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.consultation_date))
                .setSelection(selectedDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateMillis = selection
                binding.etDate.setText(formatDate(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

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

    private fun saveConsultation() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        viewModel.saveConsultation(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            dateMillis = selectedDateMillis,
            mainComplaint = binding.etComplaint.text.toString().trim(),
            recall24h = binding.etRecall.text.toString().trim(),
            evolution = binding.etEvolution.text.toString().trim()
        )
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
