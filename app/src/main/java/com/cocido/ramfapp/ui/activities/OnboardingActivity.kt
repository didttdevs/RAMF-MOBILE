package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.cocido.ramfapp.adapters.OnboardingAdapter
import com.cocido.ramfapp.databinding.ActivityOnboardingBinding
import com.cocido.ramfapp.models.OnboardingItem
import com.cocido.ramfapp.utils.SharedPreferencesManager

/**
 * Activity para el tutorial de primera vez
 */
class OnboardingActivity : BaseActivity() {
    
    override fun requiresAuthentication(): Boolean {
        return false
    }
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var sharedPrefs: SharedPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar SharedPreferencesManager después de que el contexto esté disponible
        sharedPrefs = SharedPreferencesManager(this)
        
        setupViewPager()
        setupListeners()
    }
    
    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnboardingItem(
                title = "¡Bienvenido a RAMF!",
                description = "La Red Agrometeorológica de Formosa te brinda acceso a datos meteorológicos en tiempo real de toda la provincia.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_welcome
                backgroundColor = android.R.color.holo_blue_bright // R.color.background_color_btn
            ),
            OnboardingItem(
                title = "Estaciones Meteorológicas",
                description = "Visualiza datos de más de 20 estaciones distribuidas por toda Formosa. Temperatura, humedad, precipitación y más.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_stations
                backgroundColor = android.R.color.holo_green_light // R.color.success_green
            ),
            OnboardingItem(
                title = "Mapa Interactivo",
                description = "Explora el mapa de Formosa con marcadores que muestran datos en tiempo real de cada estación meteorológica.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_map
                backgroundColor = android.R.color.holo_orange_light // R.color.warning_orange
            ),
            OnboardingItem(
                title = "Gráficos y Pronósticos",
                description = "Analiza tendencias con gráficos detallados y accede a pronósticos meteorológicos actualizados.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_charts
                backgroundColor = android.R.color.holo_blue_light // R.color.info_blue
            ),
            OnboardingItem(
                title = "Reportes y Alertas",
                description = "Crea reportes de incidencias y recibe notificaciones sobre condiciones meteorológicas importantes.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_reports
                backgroundColor = android.R.color.holo_red_light // R.color.error_red
            ),
            OnboardingItem(
                title = "¡Comencemos!",
                description = "Ya estás listo para usar RAMF. Accede a datos meteorológicos confiables y actualizados de Formosa.",
                imageRes = android.R.drawable.ic_dialog_info, // R.drawable.ic_ready
                backgroundColor = android.R.color.holo_blue_bright // R.color.background_color_btn
            )
        )
        
        onboardingAdapter = OnboardingAdapter { position ->
            if (position == onboardingItems.size - 1) {
                // Última página - ir a login
                navigateToLogin()
            } else {
                // Página siguiente
                binding.viewPager.setCurrentItem(position + 1, true)
            }
        }
        
        binding.viewPager.apply {
            adapter = onboardingAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        
        onboardingAdapter.submitList(onboardingItems)
        
        // Configurar indicador de página manual
        setupPageIndicator()
    }
    
    private fun setupListeners() {
        binding.btnSkip.setOnClickListener {
            navigateToLogin()
        }
        
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            val totalItems = onboardingAdapter.itemCount
            
            if (currentItem < totalItems - 1) {
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                navigateToLogin()
            }
        }
        
        binding.btnGetStarted.setOnClickListener {
            navigateToLogin()
        }
        
        // Listener para cambios de página
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI(position)
                updatePageIndicator(position)
            }
        })
    }
    
    private fun updateUI(position: Int) {
        val totalItems = onboardingAdapter.itemCount
        val isLastPage = position == totalItems - 1
        
        if (isLastPage) {
            binding.btnSkip.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
            binding.btnGetStarted.visibility = View.VISIBLE
        } else {
            binding.btnSkip.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            binding.btnGetStarted.visibility = View.GONE
        }
    }
    
    private fun navigateToLogin() {
        // Marcar onboarding como completado
        sharedPrefs.setOnboardingCompleted(true)
        
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun setupPageIndicator() {
        // El indicador se actualizará cuando se carguen las páginas
        updatePageIndicator(0)
    }
    
    private fun updatePageIndicator(currentPosition: Int) {
        binding.pageIndicator.removeAllViews()
        
        val totalItems = onboardingAdapter.itemCount
        for (i in 0 until totalItems) {
            val dot = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(android.R.dimen.app_icon_size), // R.dimen.dot_size
                    resources.getDimensionPixelSize(android.R.dimen.app_icon_size)  // R.dimen.dot_size
                ).apply {
                    if (i > 0) {
                        leftMargin = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) // R.dimen.dot_spacing
                    }
                }
                setBackgroundResource(
                    if (i == currentPosition) android.R.drawable.ic_dialog_alert else android.R.drawable.ic_dialog_alert // R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected
                )
            }
            binding.pageIndicator.addView(dot)
        }
    }
}
