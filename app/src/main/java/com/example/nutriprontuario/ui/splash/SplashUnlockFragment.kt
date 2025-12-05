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

/**
 * Fragment responsável pela tela de splash e desbloqueio por PIN.
 *
 * Esta é a primeira tela exibida ao abrir o aplicativo. Verifica se o usuário
 * está logado no Firebase e se possui um PIN configurado. Se o PIN estiver
 * configurado, solicita a validação antes de permitir acesso ao app.
 *
 * Fluxo de navegação:
 * - Usuário logado + sem PIN → vai direto para lista de pacientes
 * - Usuário logado + com PIN → solicita PIN, depois vai para lista
 * - Usuário não logado → vai para tela de autenticação
 */
class SplashUnlockFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentSplashUnlockBinding? = null
    // Propriedade de acesso seguro ao binding
    private val binding get() = _binding!!

    // Gerenciador de PIN local
    private lateinit var pinManager: PinManager

    /**
     * Infla o layout do fragment usando ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashUnlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Verifica o estado de autenticação e PIN para decidir o fluxo de navegação.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinManager = PinManager(requireContext())

        // Verifica se usuário está logado no Firebase
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // Usuário logado - verifica se tem PIN configurado
            if (pinManager.hasPin()) {
                promptPin() // Solicita validação do PIN
            } else {
                navigateToPatients() // Vai direto para lista
            }
        }

        // Configura botão de desbloqueio biométrico/PIN
        binding.btnBiometric.setOnClickListener {
            if (Firebase.auth.currentUser != null && pinManager.hasPin()) {
                promptPin() // Usuário logado com PIN - solicita PIN
            } else {
                // Usuário não logado - vai para autenticação
                findNavController().navigate(R.id.action_splash_to_auth)
            }
        }
    }

    /**
     * Exibe diálogo para validação do PIN.
     *
     * Se o PIN digitado for correto, navega para a lista de pacientes.
     * Caso contrário, exibe mensagem de erro.
     */
    private fun promptPin() {
        // Cria campo de texto para entrada do PIN
        val input = EditText(requireContext()).apply {
            hint = "Digite seu PIN"
            // Configura como campo numérico com máscara de senha
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        // Exibe diálogo de entrada do PIN
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.unlock)
            .setView(input)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val typed = input.text.toString()
                // Valida o PIN digitado
                if (pinManager.validate(typed)) {
                    navigateToPatients() // PIN correto - navega
                } else {
                    // PIN incorreto - exibe erro
                    Snackbar.make(binding.root, "PIN incorreto", Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Navega para a tela de lista de pacientes.
     */
    private fun navigateToPatients() {
        findNavController().navigate(R.id.action_splash_to_patientList)
    }

    /**
     * Limpa a referência do binding quando a view é destruída.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
