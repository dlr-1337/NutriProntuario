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
import com.example.nutriprontuario.databinding.FragmentPatientListBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PatientListFragment : Fragment() {

    private var _binding: FragmentPatientListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PatientAdapter
    private val viewModel: PatientListViewModel by viewModels()

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

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.authFragment, null, navOptions)
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupMenu()
        observeViewModel()
        viewModel.start(currentUser.uid)
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
                        viewModel.setQuery(newText.orEmpty())
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

    private fun observeViewModel() {
        viewModel.filtered.observe(viewLifecycleOwner) { patients ->
            adapter.submitList(patients)
            binding.tvEmpty.isVisible = patients.isEmpty()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
