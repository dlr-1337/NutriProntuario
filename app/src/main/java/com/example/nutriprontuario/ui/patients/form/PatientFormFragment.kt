package com.example.nutriprontuario.ui.patients.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.Patient
import com.example.nutriprontuario.databinding.FragmentPatientFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Fragment responsável pelo formulário de cadastro e edição de pacientes.
 *
 * Permite criar um novo paciente ou editar um existente. O modo é determinado
 * pelo argumento patientId: -1 para novo paciente, ID válido para edição.
 * Valida campos obrigatórios antes de salvar.
 */
class PatientFormFragment : Fragment() {

    // ViewBinding - referência nula quando view é destruída
    private var _binding: FragmentPatientFormBinding? = null
    // Propriedade de acesso seguro ao binding
    private val binding get() = _binding!!

    // Argumentos de navegação (patientId)
    private val args: PatientFormFragmentArgs by navArgs()
    // ViewModel para lógica do formulário
    private val viewModel: PatientFormViewModel by viewModels()

    /**
     * Infla o layout do fragment usando ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a view após sua criação.
     *
     * Configura título, botão de salvar e carrega dados se for edição.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitleAndBack()  // Configura título e navegação de volta
        binding.btnSave.setOnClickListener { savePatient() }  // Botão salvar
        observeViewModel()   // Observa mudanças no ViewModel

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

        // Carrega dados do paciente se for edição
        viewModel.loadPatient(args.patientId, currentUser.uid)
    }

    /**
     * Configura o título da tela baseado no modo (criar/editar).
     *
     * O título também serve como botão de voltar.
     */
    private fun setupTitleAndBack() {
        binding.tvTitle.text = getString(
            if (args.patientId == -1L) R.string.new_patient else R.string.edit_patient
        )
        // Clique no título volta para a tela anterior
        binding.tvTitle.setOnClickListener { findNavController().navigateUp() }
    }

    /**
     * Observa mudanças no estado do ViewModel e atualiza a UI.
     */
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            // Preenche formulário se paciente foi carregado
            if (state.patient != null) {
                fillForm(state.patient)
            }

            // Exibe erro se houver
            state.error?.let {
                Snackbar.make(
                    binding.root,
                    it.ifBlank { getString(R.string.error_generic) },
                    Snackbar.LENGTH_LONG
                ).show()
            }

            // Salvo com sucesso - volta para tela anterior
            if (state.saved) {
                Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Preenche o formulário com os dados do paciente carregado.
     *
     * Só preenche campos vazios para não sobrescrever edições do usuário.
     *
     * @param patient Paciente com os dados a serem exibidos
     */
    private fun fillForm(patient: Patient) {
        // Preenche apenas se o campo estiver vazio
        if (binding.etName.text.isNullOrBlank()) {
            binding.etName.setText(patient.name)
        }
        if (binding.etPhone.text.isNullOrBlank()) {
            binding.etPhone.setText(patient.phone)
        }
        if (binding.etNotes.text.isNullOrBlank()) {
            binding.etNotes.setText(patient.notes)
        }
    }

    /**
     * Valida e salva os dados do paciente.
     *
     * Verifica autenticação e campo obrigatório (nome) antes de salvar.
     */
    private fun savePatient() {
        // Verifica autenticação
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        // Obtém valores dos campos
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim().ifEmpty { null }
        val notes = binding.etNotes.text.toString().trim().ifEmpty { null }

        // Valida campo obrigatório
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            return
        }
        binding.tilName.error = null // Limpa erro anterior

        // Salva paciente via ViewModel
        viewModel.savePatient(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            name = name,
            phone = phone,
            notes = notes
        )
    }

    /**
     * Limpa a referência do binding quando a view é destruída.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
