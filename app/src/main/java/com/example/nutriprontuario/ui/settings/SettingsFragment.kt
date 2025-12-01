package com.example.nutriprontuario.ui.settings

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.local.PinManager
import com.example.nutriprontuario.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var pinManager: PinManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinManager = PinManager(requireContext())

        setupSecuritySettings()
        setupThemeSettings()
        setupLogout()
    }

    private fun setupSecuritySettings() {
        binding.layoutChangePin.setOnClickListener {
            showPinDialog()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) {
                "Biometria habilitada"
            } else {
                "Biometria desabilitada"
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupThemeSettings() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_theme_light -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.rb_theme_dark -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                R.id.rb_theme_system -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            Snackbar.make(binding.root, R.string.logout_success, Snackbar.LENGTH_SHORT).show()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
        }
    }

    private fun showPinDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Digite um PIN (mínimo 4 dígitos)"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_pin)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val pin = input.text.toString()
                if (pin.length < 4) {
                    Snackbar.make(binding.root, "PIN deve ter pelo menos 4 dígitos.", Snackbar.LENGTH_LONG).show()
                } else {
                    pinManager.savePin(pin)
                    Snackbar.make(binding.root, "PIN atualizado.", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
