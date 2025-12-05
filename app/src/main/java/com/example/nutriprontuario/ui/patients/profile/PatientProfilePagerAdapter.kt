package com.example.nutriprontuario.ui.patients.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientConsultationsFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientMeasurementsFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientPlansFragment
import com.example.nutriprontuario.ui.patients.profile.tabs.PatientSummaryFragment

/**
 * Adapter para o ViewPager2 na tela de perfil do paciente.
 *
 * Gerencia as 4 abas da tela de perfil:
 * - Resumo (informações gerais do paciente)
 * - Consultas (histórico de consultas)
 * - Medidas (medições antropométricas)
 * - Planos (planos alimentares)
 *
 * @param fragment O fragment pai que hospeda o ViewPager2
 * @param patientId O ID do paciente para passar aos fragments das abas
 */
class PatientProfilePagerAdapter(
    fragment: Fragment,
    private val patientId: Long
) : FragmentStateAdapter(fragment) {

    // Define o número total de abas
    override fun getItemCount(): Int = 4

    /**
     * Cria o fragment apropriado para cada posição da aba.
     *
     * @param position Posição da aba (0=Resumo, 1=Consultas, 2=Medidas, 3=Planos)
     * @return Fragment correspondente à posição
     */
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
