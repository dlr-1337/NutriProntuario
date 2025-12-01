package com.example.nutriprontuario.ui.patients.profile

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.example.nutriprontuario.R
import com.example.nutriprontuario.databinding.FragmentPatientProfileBinding
import com.google.android.material.tabs.TabLayoutMediator

class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!

    private val args: PatientProfileFragmentArgs by navArgs()

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

        setupViewPager()
        setupMenu()
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
                        // Navigate to edit patient
                        true
                    }
                    R.id.action_delete -> {
                        // Show delete confirmation
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
