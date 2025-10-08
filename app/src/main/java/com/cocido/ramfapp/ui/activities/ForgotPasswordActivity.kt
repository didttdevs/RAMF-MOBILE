package com.cocido.ramfapp.ui.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.ActivityForgotPasswordBinding
import com.cocido.ramfapp.models.ForgotPasswordRequest
import com.cocido.ramfapp.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Activity profesional para recuperación de contraseña
 * Envía email con instrucciones para resetear contraseña
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private var TAG = "ForgotPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnSendReset.setOnClickListener {
            requestPasswordReset()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun requestPasswordReset() {
        val email = binding.etEmail.text.toString().trim()

        // Validar email
        if (!isValidEmail(email)) {
            binding.tilEmail.error = "Ingresa un email válido"
            return
        }

        binding.tilEmail.error = null

        // Mostrar loading
        showLoading(true)

        // Enviar request al backend
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.requestPasswordReset(ForgotPasswordRequest(email))
                
                if (response.isSuccessful) {
                    showSuccess()
                } else {
                    showError("Error al enviar el email. Intenta nuevamente.")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSendReset.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
    }

    private fun showSuccess() {
        binding.layoutForm.visibility = View.GONE
        binding.layoutSuccess.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

