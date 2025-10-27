package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.R
import com.cocido.ramfapp.adapters.SettingsAdapter
import com.cocido.ramfapp.databinding.ActivitySettingsBinding
import com.cocido.ramfapp.models.SettingsItem
import com.cocido.ramfapp.utils.TokenManager
import com.cocido.ramfapp.utils.SharedPreferencesManager
import kotlinx.coroutines.launch

/**
 * Activity para configuraciones de la aplicación
 */
class SettingsActivity : BaseActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsAdapter: SettingsAdapter
    private lateinit var sharedPrefs: SharedPreferencesManager
    private lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar componentes
        sharedPrefs = SharedPreferencesManager(this)
        tokenManager = TokenManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadSettingsItems()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Configuraciones"
        }
    }
    
    private fun setupRecyclerView() {
        val settingsList = createSettingsList()
        settingsAdapter = SettingsAdapter(settingsList)
        
        binding.recyclerViewSettings.adapter = settingsAdapter
    }
    
    private fun createSettingsList(): List<SettingsAdapter.SettingItem> {
        return listOf(
            SettingsAdapter.SettingItem(
                title = "Notificaciones",
                description = "Recibir alertas meteorológicas",
                icon = R.drawable.ic_notifications,
                hasSwitch = true,
                switchState = true,
                onClick = { /* Toggle notification */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Tema",
                description = "Cambiar apariencia de la app",
                icon = R.drawable.ic_palette,
                hasArrow = true,
                onClick = { /* Open theme settings */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Idioma",
                description = "Seleccionar idioma",
                icon = R.drawable.ic_language,
                hasArrow = true,
                onClick = { /* Open language settings */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Sincronización",
                description = "Sincronizar datos",
                icon = R.drawable.ic_sync,
                hasArrow = true,
                onClick = { /* Sync data */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Almacenamiento",
                description = "Gestionar espacio",
                icon = R.drawable.ic_storage,
                hasArrow = true,
                onClick = { /* Open storage settings */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Soporte",
                description = "Ayuda y soporte técnico",
                icon = R.drawable.ic_support,
                hasArrow = true,
                onClick = { /* Open support */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Tutorial",
                description = "Ver tutorial de la app",
                icon = R.drawable.ic_tutorial,
                hasArrow = true,
                onClick = { /* Show tutorial */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Privacidad",
                description = "Política de privacidad",
                icon = R.drawable.ic_lock,
                hasArrow = true,
                onClick = { /* Open privacy policy */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Términos",
                description = "Términos y condiciones",
                icon = R.drawable.ic_info,
                hasArrow = true,
                onClick = { /* Open terms */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Acerca de",
                description = "Información de la app",
                icon = R.drawable.ic_info,
                hasArrow = true,
                onClick = { /* Open about */ }
            ),
            SettingsAdapter.SettingItem(
                title = "Cerrar Sesión",
                description = "Salir de la cuenta",
                icon = R.drawable.ic_logout,
                hasArrow = true,
                onClick = { /* Logout */ }
            )
        )
    }
    
    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun loadSettingsItems() {
        val settingsItems = listOf(
            // Sección: Cuenta
            SettingsItem.Section("Cuenta"),
            SettingsItem.Setting(
                title = "Perfil de Usuario",
                description = "Editar información personal y avatar",
                icon = R.drawable.ic_person,
                action = SettingsItem.Action.Navigate { navigateToProfile() }
            ),
            SettingsItem.Setting(
                title = "Cambiar Contraseña",
                description = "Actualizar contraseña de seguridad",
                icon = R.drawable.ic_lock,
                action = SettingsItem.Action.Navigate { navigateToChangePassword() }
            ),
            SettingsItem.Setting(
                title = "Eliminar Cuenta",
                description = "Eliminar permanentemente tu cuenta",
                icon = R.drawable.ic_delete,
                action = SettingsItem.Action.Navigate { navigateToDeleteAccount() }
            ),
            
            // Sección: Aplicación
            SettingsItem.Section("Aplicación"),
            SettingsItem.Setting(
                title = "Notificaciones",
                description = "Configurar alertas meteorológicas",
                icon = R.drawable.ic_notifications,
                action = SettingsItem.Action.Navigate { navigateToNotifications() }
            ),
            SettingsItem.Setting(
                title = "Tema",
                description = "Modo claro u oscuro",
                icon = R.drawable.ic_palette,
                action = SettingsItem.Action.Toggle(
                    isEnabled = sharedPrefs.isDarkThemeEnabled(),
                    onToggle = { enabled -> toggleTheme(enabled) }
                )
            ),
            SettingsItem.Setting(
                title = "Idioma",
                description = "Español (Argentina)",
                icon = R.drawable.ic_language,
                action = SettingsItem.Action.Navigate { navigateToLanguage() }
            ),
            SettingsItem.Setting(
                title = "Modo Offline",
                description = "Acceso sin conexión a internet",
                icon = R.drawable.ic_offline,
                action = SettingsItem.Action.Toggle(
                    isEnabled = sharedPrefs.isOfflineModeEnabled(),
                    onToggle = { enabled -> toggleOfflineMode(enabled) }
                )
            ),
            
            // Sección: Datos
            SettingsItem.Section("Datos"),
            SettingsItem.Setting(
                title = "Sincronización",
                description = "Sincronizar datos con el servidor",
                icon = R.drawable.ic_sync,
                action = SettingsItem.Action.Navigate { navigateToSync() }
            ),
            SettingsItem.Setting(
                title = "Almacenamiento",
                description = "Gestionar espacio de almacenamiento",
                icon = R.drawable.ic_storage,
                action = SettingsItem.Action.Navigate { navigateToStorage() }
            ),
            SettingsItem.Setting(
                title = "Exportar Datos",
                description = "Exportar datos a CSV",
                icon = R.drawable.ic_file_download,
                action = SettingsItem.Action.Navigate { navigateToExport() }
            ),
            
            // Sección: Soporte
            SettingsItem.Section("Soporte"),
            SettingsItem.Setting(
                title = "Contactar Soporte",
                description = "Enviar mensaje de ayuda",
                icon = R.drawable.ic_support,
                action = SettingsItem.Action.Navigate { navigateToContact() }
            ),
            SettingsItem.Setting(
                title = "Tutorial",
                description = "Ver tutorial de la aplicación",
                icon = R.drawable.ic_help,
                action = SettingsItem.Action.Navigate { navigateToTutorial() }
            ),
            SettingsItem.Setting(
                title = "Política de Privacidad",
                description = "Leer política de privacidad",
                icon = R.drawable.ic_lock,
                action = SettingsItem.Action.Navigate { navigateToPrivacy() }
            ),
            SettingsItem.Setting(
                title = "Términos y Condiciones",
                description = "Leer términos y condiciones",
                icon = R.drawable.ic_info,
                action = SettingsItem.Action.Navigate { navigateToTerms() }
            ),
            SettingsItem.Setting(
                title = "Acerca de",
                description = "Información de la aplicación",
                icon = R.drawable.ic_info,
                action = SettingsItem.Action.Navigate { navigateToAbout() }
            )
        )
        
        // settingsAdapter.submitList(settingsItems)
    }
    
    private fun handleSettingsItemClick(settingsItem: SettingsItem) {
        when (settingsItem) {
            is SettingsItem.Setting -> {
                when (settingsItem.action) {
                    is SettingsItem.Action.Navigate -> {
                        settingsItem.action.onClick()
                    }
                    is SettingsItem.Action.Toggle -> {
                        // El toggle se maneja automáticamente
                    }
                }
            }
            is SettingsItem.Section -> {
                // No hacer nada para las secciones
            }
        }
    }
    
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileEditActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToChangePassword() {
        val intent = Intent(this, ProfileEditActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToDeleteAccount() {
        showDeleteAccountDialog()
    }
    
    private fun navigateToNotifications() {
        // TODO: Implementar configuración de notificaciones
        Toast.makeText(this, "Configuración de notificaciones próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToLanguage() {
        // TODO: Implementar selección de idioma
        Toast.makeText(this, "Selección de idioma próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToSync() {
        // TODO: Implementar sincronización
        Toast.makeText(this, "Sincronización próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToStorage() {
        // TODO: Implementar gestión de almacenamiento
        Toast.makeText(this, "Gestión de almacenamiento próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToExport() {
        // TODO: Implementar exportación de datos
        Toast.makeText(this, "Exportación de datos próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToContact() {
        // ContactActivity eliminado - no es core para app móvil
        Toast.makeText(this, "Contacto próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToTutorial() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToPrivacy() {
        // TODO: Implementar política de privacidad
        Toast.makeText(this, "Política de privacidad próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToTerms() {
        // TODO: Implementar términos y condiciones
        Toast.makeText(this, "Términos y condiciones próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToAbout() {
        // AboutActivity eliminado - no es core para app móvil
        Toast.makeText(this, "Acerca de próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleTheme(enabled: Boolean) {
        sharedPrefs.setDarkThemeEnabled(enabled)
        // TODO: Aplicar tema inmediatamente
        Toast.makeText(this, "Tema ${if (enabled) "oscuro" else "claro"} activado", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleOfflineMode(enabled: Boolean) {
        sharedPrefs.setOfflineModeEnabled(enabled)
        Toast.makeText(this, "Modo offline ${if (enabled) "activado" else "desactivado"}", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Cuenta")
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // Limpiar tokens
                tokenManager.clearTokens()
                
                // Navegar al login
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun performDeleteAccount() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // TODO: Implementar eliminación de cuenta
                Toast.makeText(this@SettingsActivity, "Eliminación de cuenta próximamente", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al eliminar cuenta: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}