package com.example.nutriprontuario.ui.consultations

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
import com.example.nutriprontuario.databinding.FragmentConsultationFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment para cadastro e edição de consultas de pacientes.
 *
 * Permite registrar informações de uma consulta nutricional incluindo:
 * - Data da consulta
 * - Queixa principal do paciente
 * - Recordatório alimentar de 24 horas
 * - Evolução e conduta profissional
 *
 * Os dados são salvos no Firestore na subcoleção 'consultations' do paciente.
 */
class ConsultationFormFragment : Fragment() {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentConsultationFormBinding? = null
    private val binding get() = _binding!!

    // Arguments recebidos via Safe Args (contém patientId)
    private val args: ConsultationFormFragmentArgs by navArgs()
    private val viewModel: ConsultationFormViewModel by viewModels()

    // Data selecionada para a consulta (padrão: hoje)
    private var selectedDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.evolution)
        binding.btnSave.setOnClickListener { saveConsultation() }

        setupDatePicker()
        binding.etDate.setText(formatDate(selectedDateMillis))

        observeViewModel()
    }

    /**
     * Configura o seletor de data usando MaterialDatePicker.
     * Ao clicar no campo de data, exibe um calendário para seleção.
     */
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.consultation_date))
                .setSelection(selectedDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateMillis = selection
                binding.etDate.setText(formatDate(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    /**
     * Observa o estado do ViewModel para reagir a erros e sucesso no salvamento.
     * Exibe mensagens via Snackbar e navega de volta em caso de sucesso.
     */
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            state.error?.let {
                Snackbar.make(
                    binding.root,
                    it.ifBlank { getString(R.string.error_generic) },
                    Snackbar.LENGTH_LONG
                ).show()
            }
            if (state.saved) {
                Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Valida e salva os dados da consulta no Firestore.
     * Verifica se o usuário está autenticado antes de salvar.
     * Redireciona para login se a sessão expirou.
     */
    private fun saveConsultation() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // Usuário não autenticado - redireciona para login
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        // Salva a consulta com os dados do formulário
        viewModel.saveConsultation(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            dateMillis = selectedDateMillis,
            mainComplaint = binding.etComplaint.text.toString().trim(),
            recall24h = binding.etRecall.text.toString().trim(),
            evolution = binding.etEvolution.text.toString().trim()
        )
    }

    /**
     * Formata um timestamp em milissegundos para o formato dd/MM/yyyy.
     */
    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
