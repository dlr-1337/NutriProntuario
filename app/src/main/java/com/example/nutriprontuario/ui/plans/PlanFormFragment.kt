package com.example.nutriprontuario.ui.plans

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPlanFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class PlanFormFragment : Fragment() {

    private var _binding: FragmentPlanFormBinding? = null
    private val binding get() = _binding!!

    private val args: PlanFormFragmentArgs by navArgs()
    private lateinit var mealAdapter: MealSectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupDatePicker()
        setupMealSections()
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
                        savePlan()
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
                .setTitleText(getString(R.string.plan_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etDate.setText(sdf.format(Date(selection)))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupMealSections() {
        val meals = listOf(
            MealSection(getString(R.string.meal_breakfast)),
            MealSection(getString(R.string.meal_morning_snack)),
            MealSection(getString(R.string.meal_lunch)),
            MealSection(getString(R.string.meal_afternoon_snack)),
            MealSection(getString(R.string.meal_dinner)),
            MealSection(getString(R.string.meal_supper))
        )

        mealAdapter = MealSectionAdapter(meals)
        binding.rvMeals.adapter = mealAdapter
    }

    private fun savePlan() {
        // Save plan data here
        Snackbar.make(binding.root, R.string.save, Snackbar.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
