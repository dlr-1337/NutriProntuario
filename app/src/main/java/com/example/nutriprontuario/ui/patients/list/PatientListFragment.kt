package com.example.nutriprontuario.ui.patients.list

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPatientListBinding

class PatientListFragment : Fragment() {

    private var _binding: FragmentPatientListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupMenu()
        loadSampleData()
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter { patient ->
            val action = PatientListFragmentDirections
                .actionListToProfile(patient.id)
            findNavController().navigate(action)
        }
        binding.rvPatients.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_form)
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_patient_list, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = getString(R.string.search_patient)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        // Filter adapter here
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_list_to_settings)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadSampleData() {
        // Sample data for demonstration
        val samplePatients = listOf(
            Patient(1, "Maria Silva", "(62) 98765-4321", "Último atendimento: 15/01/2025"),
            Patient(2, "João Santos", "(62) 99876-5432", "Último atendimento: 10/01/2025"),
            Patient(3, "Ana Paula", "(62) 97654-3210", "Nunca")
        )
        adapter.submitList(samplePatients)
        binding.tvEmpty.isVisible = samplePatients.isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
