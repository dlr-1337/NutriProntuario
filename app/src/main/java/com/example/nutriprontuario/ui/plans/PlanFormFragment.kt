package com.example.nutriprontuario.ui.plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutriprontuario.R
import com.example.nutriprontuario.data.model.MealEntry
import com.example.nutriprontuario.databinding.FragmentPlanFormBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanFormFragment : Fragment(), PlanMealsAdapter.MealListener {

    private var _binding: FragmentPlanFormBinding? = null
    private val binding get() = _binding!!

    private val args: PlanFormFragmentArgs by navArgs()
    private val viewModel: PlanFormViewModel by viewModels()
    private lateinit var mealAdapter: PlanMealsAdapter
    private val meals = mutableListOf(
        MealUi("Cafe da Manha"),
        MealUi("Lanche da Manha"),
        MealUi("Almoco"),
        MealUi("Lanche da Tarde"),
        MealUi("Jantar"),
        MealUi("Ceia")
    )

    private var selectedDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

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

        binding.tvTitle.text = getString(R.string.new_plan)
        binding.btnSave.setOnClickListener { savePlan() }
        setupDatePicker()
        setupMealList()
        binding.etDate.setText(formatDate(selectedDateMillis))

        observeViewModel()
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.plan_date))
                .setSelection(selectedDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateMillis = selection
                binding.etDate.setText(formatDate(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupMealList() {
        mealAdapter = PlanMealsAdapter(meals, this)
        binding.rvMeals.adapter = mealAdapter
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.btnAddMeal.setOnClickListener {
            meals.add(MealUi("Nova refeicao"))
            mealAdapter.notifyItemInserted(meals.lastIndex)
            binding.rvMeals.smoothScrollToPosition(meals.lastIndex)
        }
    }

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

    private fun savePlan() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        val mealEntries = meals.map { meal ->
            MealEntry(
                name = meal.title,
                items = meal.items.joinToString("\n") { it },
                observations = meal.observations.orEmpty()
            )
        }

        viewModel.savePlan(
            patientId = args.patientId,
            ownerUid = currentUser.uid,
            dateMillis = selectedDateMillis,
            meals = mealEntries
        )
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onUpdate(mealIndex: Int, title: String?, observations: String?) {
        title?.let { meals[mealIndex].title = it }
        observations?.let { meals[mealIndex].observations = it }
    }

    override fun onAddItem(mealIndex: Int) {
        meals[mealIndex].items.add("")
        mealAdapter.notifyItemChanged(mealIndex)
    }

    override fun onRemoveItem(mealIndex: Int, itemIndex: Int) {
        if (itemIndex in meals[mealIndex].items.indices) {
            meals[mealIndex].items.removeAt(itemIndex)
            mealAdapter.notifyItemChanged(mealIndex)
        }
    }

    override fun onItemChanged(mealIndex: Int, itemIndex: Int, text: String) {
        if (itemIndex in meals[mealIndex].items.indices) {
            meals[mealIndex].items[itemIndex] = text
        }
    }

    override fun onRemoveMeal(mealIndex: Int) {
        if (mealIndex in meals.indices) {
            meals.removeAt(mealIndex)
            mealAdapter.notifyDataSetChanged()
        }
    }
}
