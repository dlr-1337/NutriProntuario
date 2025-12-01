package com.example.nutriprontuario.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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

        setupSecuritySettings()
        setupThemeSettings()
    }

    private fun setupSecuritySettings() {
        binding.layoutChangePin.setOnClickListener {
            Snackbar.make(binding.root, "Alterar PIN (funcionalidade demo)", Snackbar.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
