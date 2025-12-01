package com.example.nutriprontuario.ui.measurements

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentMeasurementFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class MeasurementFormFragment : Fragment() {

    private var _binding: FragmentMeasurementFormBinding? = null
    private val binding get() = _binding!!

    private val args: MeasurementFormFragmentArgs by navArgs()

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

        setupMenu()
        setupDatePicker()
        setupImcCalculation()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_form_confirm, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveMeasurement()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.consultation_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etDate.setText(sdf.format(Date(selection)))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupImcCalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateImc()
            }
        }

        binding.etWeight.addTextChangedListener(textWatcher)
        binding.etHeight.addTextChangedListener(textWatcher)
    }

    private fun calculateImc() {
        val weightStr = binding.etWeight.text.toString()
        val heightStr = binding.etHeight.text.toString()

        if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
            try {
                val weight = weightStr.toDouble()
                val height = heightStr.toDouble() / 100 // Convert cm to m

                if (weight > 0 && height > 0) {
                    val imc = weight / height.pow(2)
                    val classification = getImcClassification(imc)

                    binding.tvImcValue.text = String.format("%.2f", imc)
                    binding.tvImcClassification.text = classification
                }
            } catch (e: NumberFormatException) {
                resetImc()
            }
        } else {
            resetImc()
        }
    }

    private fun getImcClassification(imc: Double): String {
        return when {
            imc < 18.5 -> getString(R.string.imc_underweight)
            imc < 25.0 -> getString(R.string.imc_normal)
            imc < 30.0 -> getString(R.string.imc_overweight)
            imc < 35.0 -> getString(R.string.imc_obese1)
            imc < 40.0 -> getString(R.string.imc_obese2)
            else -> getString(R.string.imc_obese3)
        }
    }

    private fun resetImc() {
        binding.tvImcValue.text = "--"
        binding.tvImcClassification.text = "--"
    }

    private fun saveMeasurement() {
        // Save measurement data here
        Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
