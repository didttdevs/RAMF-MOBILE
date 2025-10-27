package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.RegisterRequest
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.utils.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : BaseActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }
    
    override fun requiresAuthentication(): Boolean {
        return false
    }

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d(TAG, "onCreate: RegisterActivity started")

        AuthManager.initialize(this)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()

            if (validateFields(firstName, lastName, email, password)) {
                performRegister(firstName, lastName, email, password)
            }
        }

        tvLoginLink.setOnClickListener {
            goToLoginActivity()
        }
    }

    private fun validateFields(firstName: String, lastName: String, email: String, password: String): Boolean {
        if (firstName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese su nombre", Toast.LENGTH_SHORT).show()
            return false
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese su apellido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese su email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingrese un email válido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese su contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!isValidPassword(password)) {
            Toast.makeText(this, "La contraseña debe contener al menos una mayúscula, una minúscula y un número", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
    
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }

    private fun performRegister(firstName: String, lastName: String, email: String, password: String) {
        val registerRequest = RegisterRequest(firstName, lastName, email, password)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.register(registerRequest)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Cuenta creada exitosamente. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                    goToLoginActivity()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
                        409 -> "El email ya está registrado"
                        422 -> "Datos inválidos. Verifica la información ingresada"
                        else -> "Error en registro: ${response.message()}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToLoginActivity() {
        Log.d(TAG, "goToLoginActivity: Navigating to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        Log.d(TAG, "goToMainActivity: Navigating to MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}