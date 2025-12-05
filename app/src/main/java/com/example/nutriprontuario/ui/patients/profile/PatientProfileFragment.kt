package com.example.nutriprontuario.ui.patients.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPatientProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Fragment responsável pelo perfil completo de um paciente.
 *
 * Exibe as informações do paciente em abas (ViewPager2):
 * - Resumo: informações básicas do paciente
 * - Consultas: histórico de consultas
 * - Medidas: histórico de medidas antropométricas
 * - Planos: planos alimentares prescritos
 *
 * Permite editar e excluir o paciente através do menu.
 */
class PatientProfileFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentPatientProfileBinding? = null
    // Propriedade de acesso seguro ao binding
    private val binding get() = _binding!!

    // Argumentos de navegação (patientId)
    private val args: PatientProfileFragmentArgs by navArgs()
    // ViewModel para dados do paciente
    private val viewModel: PatientDetailViewModel by viewModels()

    /**
     * Infla o layout do fragment usando ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Verifica autenticação, configura ViewPager, menu e inicia o ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verifica autenticação
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // Não autenticado - redireciona para login
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        setupViewPager()     // Configura abas
        setupMenu()          // Configura menu de opções
        observeViewModel()   // Observa erros
        viewModel.start(args.patientId, currentUser.uid) // Inicia carregamento
    }

    /**
     * Configura o ViewPager2 com as abas do perfil do paciente.
     *
     * Usa TabLayoutMediator para sincronizar TabLayout com ViewPager2.
     */
    private fun setupViewPager() {
        val adapter = PatientProfilePagerAdapter(this, args.patientId)
        binding.viewPager.adapter = adapter

        // Configura títulos das abas
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_summary)       // Resumo
                1 -> getString(R.string.tab_consultations) // Consultas
                2 -> getString(R.string.tab_measurements)  // Medidas
                3 -> getString(R.string.tab_plans)         // Planos
                else -> ""
            }
        }.attach()
    }

    /**
     * Configura o menu de opções (editar e excluir paciente).
     */
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_patient_profile, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        // Navega para edição do paciente
                        val action = PatientProfileFragmentDirections
                            .actionProfileToEditPatient(args.patientId)
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_delete -> {
                        // Exibe confirmação de exclusão
                        confirmDeletion()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Observa erros do ViewModel e exibe mensagens.
     */
    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(
                    binding.root,
                    it.ifBlank { getString(R.string.error_generic) },
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Exibe diálogo de confirmação para exclusão do paciente.
     *
     * Se confirmado, exclui o paciente e volta para a lista.
     */
    private fun confirmDeletion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_patient)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePatient { success, error ->
                    if (success) {
                        // Excluído com sucesso - volta para lista
                        Snackbar.make(binding.root, R.string.delete, Snackbar.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.patientListFragment)
                    } else {
                        // Erro na exclusão
                        Snackbar.make(
                            binding.root,
                            error ?: getString(R.string.error_generic),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
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
