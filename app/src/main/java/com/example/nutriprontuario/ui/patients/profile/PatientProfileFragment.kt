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

class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!

    private val args: PatientProfileFragmentArgs by navArgs()
    private val viewModel: PatientDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientProfileBinding.inflate(inflater, container, false)
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

        setupViewPager()
        setupMenu()
        observeViewModel()
        viewModel.start(args.patientId, currentUser.uid)
    }

    private fun setupViewPager() {
        val adapter = PatientProfilePagerAdapter(this, args.patientId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_summary)
                1 -> getString(R.string.tab_consultations)
                2 -> getString(R.string.tab_measurements)
                3 -> getString(R.string.tab_plans)
                else -> ""
            }
        }.attach()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_patient_profile, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        val action = PatientProfileFragmentDirections
                            .actionProfileToEditPatient(args.patientId)
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_delete -> {
                        confirmDeletion()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

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

    private fun confirmDeletion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_patient)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePatient { success, error ->
                    if (success) {
                        Snackbar.make(binding.root, R.string.delete, Snackbar.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.patientListFragment)
                    } else {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
