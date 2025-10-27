package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.LoginRequest
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.CreateProfileRequest
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.utils.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }
    
    override fun requiresAuthentication(): Boolean {
        // LoginActivity no requiere autenticación ya que es la pantalla de login
        return false
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: MaterialButton
    private lateinit var tvRegisterLink: TextView
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    // Activity Result Launcher para Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AuthManager.initialize(this)
        
        // Verificar si viene de sesión expirada
        checkSessionExpired()

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        // Configura el cliente de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        // Agrega el evento para el botón de Google
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Agrega el evento para ir a la pantalla de registro
        tvRegisterLink.setOnClickListener {
            goToRegisterActivity()
        }
        
    }
    
    private fun checkSessionExpired() {
        val sessionExpired = intent.getBooleanExtra("session_expired", false)
        val message = intent.getStringExtra("message")
        
        if (sessionExpired && !message.isNullOrEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun performLogin(email: String, password: String) {
        
        val loginRequest = LoginRequest(email, password)

        // Usar corrutinas para el login
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.login(loginRequest)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Guardar sesión del usuario
                        AuthManager.saveUserSession(loginResponse.user, loginResponse)

                        // Obtener datos completos del usuario desde /api/auth/me
                        lifecycleScope.launch {
                            val freshUser = AuthManager.fetchAndUpdateCurrentUser()
                            if (freshUser != null) {
                            }
                        }

                        Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Error en login: Respuesta inválida", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 -> "Cuenta deshabilitada"
                        404 -> "Usuario no encontrado"
                        else -> "Error en login: ${response.code()} - ${response.message()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Método para manejar el resultado del login con Google
    private fun handleGoogleSignInResult(resultCode: Int, data: Intent?) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando resultado de Google: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Método para manejar el resultado de Google Sign-In
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // El inicio de sesión con Google fue exitoso, ahora puedes manejar la cuenta
            val idToken = account.idToken // El token de Google para la autenticación
            // Aquí puedes enviar el idToken a tu backend para la verificación

            Toast.makeText(this, "Login exitoso con Google", Toast.LENGTH_SHORT).show()
            // Ahora guarda el usuario y token de Google
            saveGoogleUserData(account)

        } catch (e: ApiException) {

            val errorMessage = when (e.statusCode) {
                12501 -> "Login cancelado por el usuario"
                12502 -> "Error de red - Sin conexión"
                12500 -> "Error interno de Google Sign-In"
                7 -> "Error de red - Timeout"
                10 -> "Error de configuración - Verifica las credenciales de Google"
                else -> "Error de Google Sign-In (${e.statusCode}): ${e.message}"
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // Método para guardar los datos del usuario de Google
    private fun saveGoogleUserData(account: GoogleSignInAccount) {

        // Enviar datos de Google al backend
        performGoogleLogin(account)
    }

    private fun performGoogleLogin(account: GoogleSignInAccount) {
        Log.d(TAG, "performGoogleLogin: Starting Google login for email: ${account.email}")
        
        // Crear el nombre completo combinando firstName y lastName
        val fullName = buildString {
            account.givenName?.let { append(it) }
            if (!account.givenName.isNullOrEmpty() && !account.familyName.isNullOrEmpty()) {
                append(" ")
            }
            account.familyName?.let { append(it) }
        }.ifEmpty { account.displayName ?: "" }

        // Crear el body con los datos de Google que necesita el backend
        val googleToken = mapOf(
            "email" to (account.email ?: ""),
            "name" to fullName,  // Enviar nombre completo aquí
            "firstName" to (account.givenName ?: ""),
            "lastName" to (account.familyName ?: ""),
            "avatar" to (account.photoUrl?.toString() ?: ""),
            // FORMATO 1: Usar google_id (como en los ejemplos de Postman)
            "google_id" to (account.id ?: ""),
            // FORMATO 2: Usar idToken (como está actualmente)
            "idToken" to (account.idToken ?: "")
        )
        

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.googleLogin(googleToken)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d(TAG, "Google login successful for user: ${loginResponse.user.email}")
                        Log.d(TAG, "Backend returned user:")
                        Log.d(TAG, "  id: ${loginResponse.user.id}")
                        Log.d(TAG, "  email: ${loginResponse.user.email}")
                        Log.d(TAG, "  name: ${loginResponse.user.name}")
                        Log.d(TAG, "  lastName: ${loginResponse.user.lastName}")
                        Log.d(TAG, "  avatar: ${loginResponse.user.avatar}")

                        // Sobrescribir el nombre con el nombre completo de Google
                        // ya que el backend no lo guarda correctamente
                        val correctedUser = loginResponse.user.copy(
                            name = account.givenName ?: loginResponse.user.name,
                            lastName = account.familyName ?: loginResponse.user.lastName
                        )

                        Log.d(TAG, "Corrected user with Google data: name='$fullName'")

                        // Guardar sesión del usuario con datos corregidos
                        AuthManager.saveUserSession(correctedUser, loginResponse)

                        // Obtener datos completos del usuario desde /api/auth/me
                        lifecycleScope.launch {
                            val freshUser = AuthManager.fetchAndUpdateCurrentUser()
                            if (freshUser != null) {
                                Log.d(TAG, "User data updated from /api/auth/me: ${freshUser.getFullName()}")
                            } else {
                                Log.w(TAG, "Could not fetch fresh user data, using login response data")
                            }
                        }

                        Toast.makeText(this@LoginActivity, "Login exitoso con Google", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {
                        Log.e(TAG, "Google login failed: Response body is null")
                        Toast.makeText(this@LoginActivity, "Error: Respuesta vacía del servidor", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e(TAG, "Google login failed: HTTP ${response.code()} - ${response.message()}")
                    val errorMessage = when (response.code()) {
                        401 -> "Error de autenticación con Google"
                        403 -> "Cuenta Google no autorizada"
                        404 -> "Usuario no encontrado"
                        500 -> "Error interno del servidor"
                        else -> "Error en login con Google: ${response.code()} - ${response.message()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google login exception", e)
                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun goToMainActivity() {
        Log.d(TAG, "goToMainActivity: Navigating to MainActivity")
        try {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            Log.d(TAG, "goToMainActivity: Navigation successful")
        } catch (e: Exception) {
            Log.e(TAG, "goToMainActivity: Error navigating to MainActivity", e)
        }
    }

    private fun goToRegisterActivity() {
        Log.d(TAG, "goToRegisterActivity: Navigating to RegisterActivity")
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}