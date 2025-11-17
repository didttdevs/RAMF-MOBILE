package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cocido.ramfapp.ui.components.showErrorMessage
import com.cocido.ramfapp.ui.components.showInfoMessage

/**
 * Activity base con verificación automática de sesión
 * Todas las Activities que requieren autenticación deben extender de esta
 */
abstract class BaseActivity : AppCompatActivity() {
    
    companion object {
        private const val SESSION_CHECK_INTERVAL = 30000L // 30 segundos
    }
    
    private var sessionCheckJob: kotlinx.coroutines.Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar sesión al crear la activity
        checkSessionOnCreate()
    }
    
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        
        // Configurar edge-to-edge después de que la vista esté lista
        enableEdgeToEdge()
    }
    
    /**
     * Configurar edge-to-edge con barras transparentes para todas las activities
     */
    protected fun enableEdgeToEdge() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.statusBars() or androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Verificar sesión al volver a la activity
        checkSessionOnResume()
        
        // Iniciar verificación periódica de sesión
        startSessionCheck()
    }
    
    override fun onPause() {
        super.onPause()
        
        // Detener verificación periódica
        stopSessionCheck()
    }
    
    /**
     * Verificar sesión al crear la activity
     */
    private fun checkSessionOnCreate() {
        // Skip authentication check si la activity no requiere autenticación
        if (!requiresAuthentication()) {
            return
        }
        
        // Si estamos en login/register/etc, no hacer verificación de sesión
        if (this is LoginActivity || this is RegisterActivity || 
            this is ForgotPasswordActivity || this is ResetPasswordActivity) {
            return
        }
        
        if (!AuthManager.isUserLoggedIn()) {
            redirectToLogin("Sesión expirada. Por favor, inicia sesión nuevamente.")
            return
        }
        
        // Verificar si la sesión ha expirado
        if (AuthManager.checkSessionExpiry()) {
            redirectToLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.")
            return
        }
    }
    
    /**
     * Verificar sesión al volver a la activity
     */
    private fun checkSessionOnResume() {
        // Skip authentication check si la activity no requiere autenticación
        if (!requiresAuthentication()) {
            return
        }
        
        // Si estamos en login/register/etc, no hacer verificación de sesión
        if (this is LoginActivity || this is RegisterActivity || 
            this is ForgotPasswordActivity || this is ResetPasswordActivity) {
            return
        }
        
        if (!AuthManager.isUserLoggedIn()) {
            redirectToLogin("Sesión expirada. Por favor, inicia sesión nuevamente.")
            return
        }
        
        // Verificar si la sesión ha expirado
        if (AuthManager.checkSessionExpiry()) {
            redirectToLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.")
            return
        }
    }
    
    /**
     * Iniciar verificación periódica de sesión
     */
    private fun startSessionCheck() {
        // Solo iniciar verificación si la activity requiere autenticación
        if (!requiresAuthentication()) {
            return
        }
        
        // No iniciar verificación en Login/Register/etc
        if (this is LoginActivity || this is RegisterActivity || 
            this is ForgotPasswordActivity || this is ResetPasswordActivity) {
            return
        }
        
        sessionCheckJob = lifecycleScope.launch {
            while (true) {
                delay(SESSION_CHECK_INTERVAL)
                
                // Skip si ya no requiere autenticación (por cambios en el estado)
                if (!requiresAuthentication()) {
                    break
                }
                
                // Verificar si la sesión ha expirado
                if (AuthManager.checkSessionExpiry()) {
                    redirectToLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.")
                    break
                }
                
                // Verificar si el token necesita ser refrescado
                if (AuthManager.isTokenExpiringSoon()) {
                    val refreshed = AuthManager.refreshTokenIfNeeded()
                    if (!refreshed) {
                        redirectToLogin("No se pudo renovar la sesión. Por favor, inicia sesión nuevamente.")
                        break
                    }
                }
            }
        }
    }
    
    /**
     * Detener verificación periódica de sesión
     */
    private fun stopSessionCheck() {
        sessionCheckJob?.cancel()
        sessionCheckJob = null
    }
    
    /**
     * Redirigir al login con mensaje
     */
    protected fun redirectToLogin(message: String? = null) {
        // Detener verificación de sesión
        stopSessionCheck()
        
        // Mostrar mensaje si se proporciona
        message?.let {
            showInfoMessage(it)
        }
        
        // Limpiar stack de activities y redirigir al login
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("session_expired", true)
            putExtra("message", message)
        }
        
        startActivity(intent)
        finish()
    }
    
    /**
     * Verificar si la activity actual requiere autenticación
     * Override en Activities que no requieren autenticación
     */
    protected open fun requiresAuthentication(): Boolean {
        return true
    }
    
    /**
     * Manejar error de autenticación
     */
    protected fun handleAuthError(errorMessage: String) {
        showErrorMessage(errorMessage)
        redirectToLogin(errorMessage)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopSessionCheck()
    }
}

