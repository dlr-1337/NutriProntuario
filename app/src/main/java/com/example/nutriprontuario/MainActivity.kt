package com.example.nutriprontuario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriprontuario.databinding.ActivityMainBinding

/**
 * Activity principal e única do aplicativo NutriProntuário.
 *
 * Esta Activity serve como container para o Navigation Component,
 * que gerencia a navegação entre todos os Fragments do aplicativo.
 * Segue o padrão Single Activity Architecture recomendado pelo Android.
 *
 * O layout contém um NavHostFragment que hospeda todos os destinos
 * definidos no nav_graph.xml.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acesso seguro às views do layout
    private lateinit var binding: ActivityMainBinding

    /**
     * Chamado quando a Activity é criada.
     *
     * Infla o layout usando ViewBinding e configura como content view.
     * O NavHostFragment no layout gerencia automaticamente a navegação.
     *
     * @param savedInstanceState Estado salvo da instância anterior (se houver)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mantém a tela imersiva aplicando os insets das barras do sistema para não sobrepor status e navegação
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Infla o layout usando ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }
}
