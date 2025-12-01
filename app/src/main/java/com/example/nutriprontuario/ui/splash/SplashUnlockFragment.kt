package com.example.nutriprontuario.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentSplashUnlockBinding

class SplashUnlockFragment : Fragment() {

    private var _binding: FragmentSplashUnlockBinding? = null
    private val binding get() = _binding!!

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

        binding.btnBiometric.setOnClickListener {
            // For demo purposes, navigate directly to patient list
            // In a real app, implement biometric authentication here
            findNavController().navigate(R.id.action_splash_to_patientList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
