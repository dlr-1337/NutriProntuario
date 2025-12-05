package com.example.nutriprontuario.ui.patients.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.Patient
import com.example.nutriprontuario.databinding.FragmentPatientListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Fragment responsável pela tela principal de lista de pacientes.
 *
 * Exibe todos os pacientes cadastrados pelo nutricionista logado em uma
 * RecyclerView com opções de busca, edição e exclusão. Também permite
 * navegar para o cadastro de novos pacientes e configurações do app.
 */
class PatientListFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentPatientListBinding? = null
    // Propriedade de acesso seguro ao binding
    private val binding get() = _binding!!

    // Adapter para a lista de pacientes
    private lateinit var adapter: PatientAdapter
    // ViewModel para gerenciar dados da lista
    private val viewModel: PatientListViewModel by viewModels()

    /**
     * Infla o layout do fragment usando ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Verifica autenticação, configura toolbar, RecyclerView, FAB,
     * menu e inicia a observação do ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verifica se usuário está autenticado
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // Não autenticado - redireciona para login
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        setupToolbar()       // Configura a toolbar
        setupRecyclerView()  // Configura a lista de pacientes
        setupFab()           // Configura botão flutuante
        setupMenu()          // Configura menu de opções
        observeViewModel()   // Observa mudanças no ViewModel
        viewModel.start(currentUser.uid) // Inicia carregamento dos dados
    }

    /**
     * Configura a toolbar como ActionBar da Activity.
     */
    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }

    /**
     * Configura o RecyclerView com o adapter de pacientes.
     *
     * Define callbacks para clique no item, edição e exclusão.
     */
    private fun setupRecyclerView() {
        adapter = PatientAdapter(
            onItemClick = { patient ->
                // Clique no paciente - navega para perfil
                val action = PatientListFragmentDirections
                    .actionListToProfile(patient.id)
                findNavController().navigate(action)
            },
            onEdit = { patient ->
                // Clique em editar - navega para formulário de edição
                val action = PatientListFragmentDirections.actionListToForm(patient.id)
                findNavController().navigate(action)
            },
            onDelete = { patient ->
                // Clique em excluir - exibe confirmação
                confirmDelete(patient)
            }
        )
        binding.rvPatients.adapter = adapter
    }

    /**
     * Configura o FloatingActionButton para adicionar novo paciente.
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Navega para formulário com ID -1 (novo paciente)
            val action = PatientListFragmentDirections.actionListToForm(-1L)
            findNavController().navigate(action)
        }
    }

    /**
     * Configura o menu de opções (busca e configurações).
     *
     * Usa MenuProvider para adicionar menu apenas quando o fragment está ativo.
     */
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_patient_list, menu)

                // Configura SearchView para busca de pacientes
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = getString(R.string.search_patient)

                // Listener para mudanças no texto de busca
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        // Atualiza filtro no ViewModel
                        viewModel.setQuery(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        // Navega para configurações
                        findNavController().navigate(R.id.action_list_to_settings)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Observa mudanças nos dados do ViewModel e atualiza a UI.
     */
    private fun observeViewModel() {
        // Observa lista filtrada de pacientes
        viewModel.filtered.observe(viewLifecycleOwner) { patients ->
            adapter.submitList(patients)
            // Mostra mensagem de lista vazia se não houver pacientes
            binding.tvEmpty.isVisible = patients.isEmpty()
        }

        // Observa erros
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
     * Exibe diálogo de confirmação para exclusão de paciente.
     *
     * @param patient Paciente a ser excluído
     */
    private fun confirmDelete(patient: Patient) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_patient)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                // Confirma exclusão - chama ViewModel
                viewModel.deletePatient(patient.id) { success, error ->
                    val msg = if (success) {
                        getString(R.string.delete)
                    } else {
                        error ?: getString(R.string.error_generic)
                    }
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
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
