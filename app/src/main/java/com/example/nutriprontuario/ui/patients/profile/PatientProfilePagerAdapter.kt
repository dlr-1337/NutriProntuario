package com.example.nutriprontuario.ui.patients.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientConsultationsFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientMeasurementsFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientPlansFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientSummaryFragment

class PatientProfilePagerAdapter(
    fragment: Fragment,
    private val patientId: Long
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PatientSummaryFragment.newInstance(patientId)
            1 -> PatientConsultationsFragment.newInstance(patientId)
            2 -> PatientMeasurementsFragment.newInstance(patientId)
            3 -> PatientPlansFragment.newInstance(patientId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
