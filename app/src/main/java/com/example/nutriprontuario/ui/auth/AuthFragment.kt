package com.example.nutriprontuario.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentAuthBinding
import com.example.nutriprontuario.ui.auth.AuthViewModel.AuthState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Firebase.auth.currentUser != null) {
            navigateToPatients()
            return
        }

        binding.btnSignIn.setOnClickListener { handleSignIn() }
        binding.btnSignUp.setOnClickListener { handleSignUp() }

        observeState()
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: AuthState) {
        binding.progressAuth.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !state.isLoading
        binding.btnSignUp.isEnabled = !state.isLoading

        if (state.error != null) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = state.error
        } else {
            binding.tvError.visibility = View.GONE
        }

        if (state.success) {
            navigateToPatients()
        }
    }

    private fun handleSignIn() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        viewModel.signIn(email, password)
    }

    private fun handleSignUp() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        viewModel.signUp(email, password)
    }

    private fun navigateToPatients() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.splashUnlockFragment, true)
            .build()
        findNavController().navigate(R.id.patientListFragment, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
