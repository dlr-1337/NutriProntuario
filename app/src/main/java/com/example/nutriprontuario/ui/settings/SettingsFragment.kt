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

/**
 * Fragment responsável pela tela de configurações do aplicativo.
 *
 * Permite ao usuário:
 * - Configurar/alterar o PIN de acesso local
 * - Habilitar/desabilitar autenticação biométrica
 * - Escolher o tema do aplicativo (claro, escuro ou sistema)
 * - Fazer logout da conta Firebase
 */
class SettingsFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentSettingsBinding? = null
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Inicializa o gerenciador de PIN e configura todas as seções
     * de configuração (segurança, tema e logout).
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinManager = PinManager(requireContext())

        setupSecuritySettings() // Configura seção de segurança
        setupThemeSettings()    // Configura seção de tema
        setupLogout()           // Configura botão de logout
    }

    /**
     * Configura as opções de segurança (PIN e biometria).
     *
     * Permite alterar o PIN e habilitar/desabilitar autenticação biométrica.
     */
    private fun setupSecuritySettings() {
        // Clique para alterar PIN
        binding.layoutChangePin.setOnClickListener {
            showPinDialog()
        }

        // Switch para habilitar/desabilitar biometria
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) {
                "Biometria habilitada"
            } else {
                "Biometria desabilitada"
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura as opções de tema do aplicativo.
     *
     * Permite escolher entre tema claro, escuro ou seguir o sistema.
     * Usa AppCompatDelegate para aplicar o tema globalmente.
     */
    private fun setupThemeSettings() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_theme_light -> {
                    // Tema claro
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.rb_theme_dark -> {
                    // Tema escuro
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                R.id.rb_theme_system -> {
                    // Seguir configuração do sistema
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    /**
     * Configura o botão de logout.
     *
     * Faz logout do Firebase e navega de volta para a tela de autenticação,
     * removendo todas as telas anteriores da pilha de navegação.
     */
    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            // Faz logout do Firebase
            Firebase.auth.signOut()
            Snackbar.make(binding.root, R.string.logout_success, Snackbar.LENGTH_SHORT).show()

            // Navega para auth limpando toda a pilha
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // Remove toda a pilha
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
        }
    }

    /**
     * Exibe diálogo para configurar/alterar o PIN.
     *
     * Valida se o PIN tem pelo menos 4 dígitos antes de salvar.
     */
    private fun showPinDialog() {
        // Cria campo de texto para entrada do PIN
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Digite um PIN (mínimo 4 dígitos)"
        }

        // Exibe diálogo
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_pin)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val pin = input.text.toString()
                // Valida tamanho mínimo do PIN
                if (pin.length < 4) {
                    Snackbar.make(
                        binding.root,
                        "PIN deve ter pelo menos 4 dígitos.",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    // Salva o novo PIN
                    pinManager.savePin(pin)
                    Snackbar.make(binding.root, "PIN atualizado.", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Limpa a referência do binding quando a view é destruída.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
