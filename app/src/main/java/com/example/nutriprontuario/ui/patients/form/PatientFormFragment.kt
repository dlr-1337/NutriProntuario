package com.example.nutriprontuario.ui.patients.form

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPatientFormBinding
import com.google.android.material.snackbar.Snackbar

class PatientFormFragment : Fragment() {

    private var _binding: FragmentPatientFormBinding? = null
    private val binding get() = _binding!!

    private val args: PatientFormFragmentArgs by navArgs()

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

        setupMenu()
        loadPatientData()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_form_confirm, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        savePatient()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadPatientData() {
        if (args.patientId != -1L) {
            // Load patient data for editing
            // For demo, just show empty form
        }
    }

    private fun savePatient() {
        val name = binding.etName.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            return
        }

        binding.tilName.error = null

        // Save patient data here
        Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
