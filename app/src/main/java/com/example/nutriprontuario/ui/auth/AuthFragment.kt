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

/**
 * Fragment responsável pela tela de autenticação (login e cadastro).
 *
 * Permite que o usuário faça login com email/senha ou crie uma nova conta.
 * Usa Firebase Authentication para gerenciar as credenciais.
 * Se o usuário já estiver logado, navega automaticamente para a lista de pacientes.
 */
class AuthFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentAuthBinding? = null
    // Propriedade de acesso seguro ao binding (só usar entre onCreateView e onDestroyView)
    private val binding get() = _binding!!

    // ViewModel para lógica de autenticação (injetado automaticamente)
    private val viewModel: AuthViewModel by viewModels()

    /**
     * Infla o layout do fragment usando ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Verifica se o usuário já está logado e configura os listeners
     * dos botões de login e cadastro.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se já está logado, vai direto para lista de pacientes
        if (Firebase.auth.currentUser != null) {
            navigateToPatients()
            return
        }

        // Configura listeners dos botões
        binding.btnSignIn.setOnClickListener { handleSignIn() }
        binding.btnSignUp.setOnClickListener { handleSignUp() }

        // Observa mudanças no estado da autenticação
        observeState()
    }

    /**
     * Observa o estado do ViewModel e atualiza a UI quando necessário.
     */
    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    /**
     * Renderiza a UI baseada no estado atual da autenticação.
     *
     * Mostra/esconde o progress, habilita/desabilita botões,
     * exibe mensagens de erro e navega em caso de sucesso.
     *
     * @param state Estado atual da autenticação
     */
    private fun render(state: AuthState) {
        // Mostra/esconde indicador de carregamento
        binding.progressAuth.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Desabilita botões durante carregamento
        binding.btnSignIn.isEnabled = !state.isLoading
        binding.btnSignUp.isEnabled = !state.isLoading

        // Exibe mensagem de erro se houver
        if (state.error != null) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = state.error
        } else {
            binding.tvError.visibility = View.GONE
        }

        // Navega para lista de pacientes se login/cadastro foi bem-sucedido
        if (state.success) {
            navigateToPatients()
        }
    }

    /**
     * Processa o clique no botão de login.
     *
     * Obtém email e senha dos campos de texto e solicita login ao ViewModel.
     */
    private fun handleSignIn() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        viewModel.signIn(email, password)
    }

    /**
     * Processa o clique no botão de cadastro.
     *
     * Obtém email e senha dos campos de texto e solicita cadastro ao ViewModel.
     */
    private fun handleSignUp() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        viewModel.signUp(email, password)
    }

    /**
     * Navega para a tela de lista de pacientes.
     *
     * Remove as telas anteriores da pilha (splash/auth) para que
     * o botão voltar não retorne para a autenticação.
     */
    private fun navigateToPatients() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.splashUnlockFragment, true) // Remove splash da pilha
            .build()
        findNavController().navigate(R.id.patientListFragment, null, navOptions)
    }

    /**
     * Limpa a referência do binding quando a view é destruída.
     *
     * Importante para evitar vazamento de memória.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
