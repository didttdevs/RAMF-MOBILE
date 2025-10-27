package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.R
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.launch

/**
 * SplashActivity con mejores prácticas de seguridad y UX
 */
class SplashActivity : BaseActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
    
    override fun requiresAuthentication(): Boolean {
        return false
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        Log.d(TAG, "SplashActivity started")
        
        // Initialize AuthManager
        AuthManager.initialize(this)
        
        // Verificar si el token necesita ser refrescado
        checkTokenAndNavigate()
    }
    
    private fun checkTokenAndNavigate() {
        lifecycleScope.launch {
            try {
                // Verificar si el usuario está logueado y si el token necesita refrescarse
                if (AuthManager.isUserLoggedIn()) {
                    Log.d(TAG, "User is logged in, checking token status")
                    
                    // Verificar si la sesión ha expirado completamente
                    if (AuthManager.checkSessionExpiry()) {
                        Log.w(TAG, "Session has expired, redirecting to login")
                        navigateToLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.")
                        return@launch
                    }
                    
                    if (AuthManager.isTokenExpiringSoon()) {
                        Log.d(TAG, "Token is expiring soon, attempting refresh")
                        val refreshSuccess = AuthManager.refreshTokenIfNeeded()
                        
                        if (!refreshSuccess) {
                            Log.w(TAG, "Token refresh failed, user will need to login again")
                            navigateToLogin("No se pudo renovar la sesión. Por favor, inicia sesión nuevamente.")
                            return@launch
                        }
                    }
                } else {
                    Log.d(TAG, "User is not logged in")
                }
                
                // Navegar después del delay
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextScreen()
                }, SPLASH_DELAY)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during token check", e)
                
                // En caso de error, navegar de todas formas
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextScreen()
                }, SPLASH_DELAY)
            }
        }
    }
    
    private fun navigateToNextScreen() {
        val intent = if (AuthManager.isUserLoggedIn()) {
            Log.d(TAG, "Navigating to MainActivity (user logged in)")
            Intent(this, MainActivity::class.java)
        } else {
            Log.d(TAG, "Navigating to LoginActivity (user not logged in)")
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    private fun navigateToLogin(message: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("session_expired", true)
            putExtra("message", message)
        }
        
        startActivity(intent)
        finish()
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}