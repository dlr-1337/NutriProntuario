package com.example.nutriprontuario.ui.measurements

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentMeasurementFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment para cadastro de medições antropométricas de pacientes.
 *
 * Permite registrar:
 * - Data da medição
 * - Peso (kg)
 * - Altura (cm)
 * - Circunferência da cintura (cm)
 *
 * O IMC é calculado automaticamente em tempo real conforme o usuário digita
 * peso e altura, exibindo também a classificação (ex: normal, sobrepeso).
 * Os dados são salvos no Firestore na subcoleção 'measurements' do paciente.
 */
class MeasurementFormFragment : Fragment() {

    // ViewBinding para acessar as views do layout
    private var _binding: FragmentMeasurementFormBinding? = null
    private val binding get() = _binding!!

    // Arguments recebidos via Safe Args (contém patientId)
    private val args: MeasurementFormFragmentArgs by navArgs()
    private val viewModel: MeasurementFormViewModel by viewModels()

    // Data selecionada para a medição (padrão: hoje)
    private var selectedDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasurementFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.new_measurement)
        binding.btnSave.setOnClickListener { saveMeasurement() }

        setupDatePicker()
        binding.etDate.setText(formatDate(selectedDateMillis))
        setupImcCalculation()

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
     * Configura o cálculo automático do IMC em tempo real.
     * Monitora mudanças nos campos de peso e altura e recalcula o IMC
     * através do ViewModel sempre que os valores mudam.
     */
    private fun setupImcCalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val weight = binding.etWeight.text.toString().toDoubleOrNull()
                val height = binding.etHeight.text.toString().toDoubleOrNull()
                // Recalcula o IMC no ViewModel
                viewModel.calculateImc(weight, height)
            }
        }

        binding.etWeight.addTextChangedListener(textWatcher)
        binding.etHeight.addTextChangedListener(textWatcher)
    }

    /**
     * Observa o estado do ViewModel para atualizar o IMC calculado e reagir a erros/sucesso.
     * Exibe o valor do IMC e sua classificação em tempo real.
     */
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            // Atualiza os campos de IMC e classificação
            binding.tvImcValue.text = state.imc
            binding.tvImcClassification.text = state.classification

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
     * Valida e salva os dados da medição no Firestore.
     * Verifica se o usuário está autenticado antes de salvar.
     * Redireciona para login se a sessão expirou.
     */
    private fun saveMeasurement() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // Usuário não autenticado - redireciona para login
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        // Converte os valores dos campos para Double
        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        val height = binding.etHeight.text.toString().toDoubleOrNull()
        val waist = binding.etWaist.text.toString().toDoubleOrNull()

        // Salva a medição com os dados do formulário
        viewModel.saveMeasurement(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            dateMillis = selectedDateMillis,
            weight = weight,
            heightCm = height,
            waistCm = waist
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
