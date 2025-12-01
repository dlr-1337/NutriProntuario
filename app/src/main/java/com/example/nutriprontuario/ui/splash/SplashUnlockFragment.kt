package com.example.nutriprontuario.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.local.PinManager
import com.example.nutriprontuario.databinding.FragmentSplashUnlockBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashUnlockFragment : Fragment() {

    private var _binding: FragmentSplashUnlockBinding? = null
    private val binding get() = _binding!!
    private lateinit var pinManager: PinManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashUnlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinManager = PinManager(requireContext())

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            if (pinManager.hasPin()) {
                promptPin()
            } else {
                navigateToPatients()
            }
        }

        binding.btnBiometric.setOnClickListener {
            if (Firebase.auth.currentUser != null && pinManager.hasPin()) {
                promptPin()
            } else {
                findNavController().navigate(R.id.action_splash_to_auth)
            }
        }
    }

    private fun promptPin() {
        val input = EditText(requireContext()).apply {
            hint = "Digite seu PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.unlock)
            .setView(input)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val typed = input.text.toString()
                if (pinManager.validate(typed)) {
                    navigateToPatients()
                } else {
                    Snackbar.make(binding.root, "PIN incorreto", Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun navigateToPatients() {
        findNavController().navigate(R.id.action_splash_to_patientList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
