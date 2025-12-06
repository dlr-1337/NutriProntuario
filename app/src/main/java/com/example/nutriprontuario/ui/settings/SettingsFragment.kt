package com.example.nutriprontuario.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Fragment responsável pela tela de configurações do aplicativo.
 *
 * Permite ao usuário:
 * - Escolher o tema do aplicativo (claro, escuro ou sistema)
 * - Fazer logout da conta Firebase
 */
class SettingsFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentSettingsBinding? = null
    // Propriedade de acesso seguro ao binding
    private val binding get() = _binding!!

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
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSettings()    // Configura seção de tema
        setupLogout()           // Configura botão de logout
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
     * Limpa a referência do binding quando a view é destruída.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
