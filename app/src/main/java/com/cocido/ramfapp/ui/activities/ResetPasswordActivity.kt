package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.ActivityResetPasswordBinding
import com.cocido.ramfapp.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Activity para resetear contraseña con token del email
 * Maneja deep linking desde el email de recuperación
 */
class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private var resetToken: String? = null
    
    override fun requiresAuthentication(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        handleIntent()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }

        binding.btnBackToLoginForm.setOnClickListener {
            finish()
        }
    }

    private fun handleIntent() {
        // Manejar deep linking desde email
        val intent = intent
        val data: Uri? = intent.data
        
        if (data != null) {
            // Extraer token de la URL
            resetToken = data.getQueryParameter("token")
            if (resetToken.isNullOrBlank()) {
                showError("Token de recuperación inválido")
                finish()
                return
            }
        } else {
            // Si no hay deep link, intentar obtener token de intent extra
            resetToken = intent.getStringExtra("reset_token")
            if (resetToken.isNullOrBlank()) {
                showError("No se encontró token de recuperación")
                finish()
                return
            }
        }
    }

    private fun resetPassword() {
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validar contraseñas
        if (!validatePasswords(newPassword, confirmPassword)) {
            return
        }

        if (resetToken.isNullOrBlank()) {
            showError("Token de recuperación no disponible")
            return
        }

        // Mostrar loading
        showLoading(true)

        // Resetear contraseña
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.resetPassword(
                    mapOf(
                        "token" to resetToken!!,
                        "newPassword" to newPassword
                    )
                )
                
                if (response.isSuccessful) {
                    showSuccess()
                } else {
                    when (response.code()) {
                        400 -> showError("Token inválido o expirado")
                        422 -> showError("La contraseña no cumple los requisitos")
                        else -> showError("Error al resetear contraseña. Intenta nuevamente.")
                    }
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validar nueva contraseña
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "La nueva contraseña es requerida"
            isValid = false
        } else if (newPassword.length < 8) {
            binding.tilNewPassword.error = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else if (!isValidPassword(newPassword)) {
            binding.tilNewPassword.error = "Debe contener mayúscula, minúscula y número"
            isValid = false
        } else {
            binding.tilNewPassword.error = null
        }

        // Validar confirmación
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirma tu nueva contraseña"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUpperCase && hasLowerCase && hasDigit
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnResetPassword.isEnabled = !loading
        binding.etNewPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }

    private fun showSuccess() {
        binding.layoutForm.visibility = View.GONE
        binding.layoutSuccess.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun startWithToken(activity: BaseActivity, token: String) {
            val intent = Intent(activity, ResetPasswordActivity::class.java).apply {
                putExtra("reset_token", token)
            }
            activity.startActivity(intent)
        }
    }
}
