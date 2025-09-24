package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.LoginRequest
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.User
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

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: FrameLayout
    private lateinit var tvRegisterLink: TextView
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    // Para el código de resultado de la actividad de Google
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate: LoginActivity started")

        AuthManager.initialize(this)
        Log.d(TAG, "onCreate: AuthManager initialized")

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        // Configura el cliente de Google Sign-In
        Log.d(TAG, "onCreate: Configuring Google Sign-In")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        Log.d(TAG, "onCreate: GoogleSignInOptions created")

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d(TAG, "onCreate: GoogleSignInClient created")

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
            Log.d(TAG, "onCreate: Google button clicked")
            signInWithGoogle()
        }

        // Agrega el evento para ir a la pantalla de registro
        tvRegisterLink.setOnClickListener {
            goToRegisterActivity()
        }
        
        Log.d(TAG, "onCreate: All components initialized")
    }

    private fun performLogin(email: String, password: String) {
        Log.d(TAG, "performLogin: Starting login process for email: $email")
        
        val loginRequest = LoginRequest(email, password)

        // Usar corrutinas para el login
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.login(loginRequest)
                val result = RetrofitClient.handleApiResponse(response)
                
                result.onSuccess { loginResponse ->
                    Log.d(TAG, "Login successful for user: ${loginResponse.user.email}")
                    
                    // Guardar sesión del usuario
                    AuthManager.saveUserSession(loginResponse.user, loginResponse)
                    
                    Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                }.onFailure { exception ->
                    Log.e(TAG, "Login failed", exception)
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> "Credenciales incorrectas"
                        exception.message?.contains("403") == true -> "Cuenta deshabilitada"
                        exception.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                        else -> "Error en login: ${exception.message}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        Log.d(TAG, "signInWithGoogle: Starting Google Sign-In")
        try {
            val signInIntent = googleSignInClient.signInIntent
            Log.d(TAG, "signInWithGoogle: Got sign-in intent")
            startActivityForResult(signInIntent, RC_SIGN_IN)
            Log.d(TAG, "signInWithGoogle: Started activity for result")
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle: Error starting Google Sign-In", e)
            Toast.makeText(this, "Error al iniciar Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Método para manejar el resultado del login con Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        // Si el código de la solicitud es el que esperamos (RC_SIGN_IN)
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Google Sign-In result received")
            try {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                Log.d(TAG, "onActivityResult: Got task from intent")
                handleSignInResult(task)
            } catch (e: Exception) {
                Log.e(TAG, "onActivityResult: Error getting task from intent", e)
                Toast.makeText(this, "Error procesando resultado de Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG, "onActivityResult: Request code doesn't match RC_SIGN_IN ($RC_SIGN_IN)")
        }
    }

    // Método para manejar el resultado de Google Sign-In
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Log.d(TAG, "handleSignInResult: Processing Google Sign-In result")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "handleSignInResult: Successfully got GoogleSignInAccount")
            Log.d(TAG, "handleSignInResult: Account email: ${account.email}")
            Log.d(TAG, "handleSignInResult: Account name: ${account.displayName}")
            Log.d(TAG, "handleSignInResult: Account ID: ${account.id}")

            // El inicio de sesión con Google fue exitoso, ahora puedes manejar la cuenta
            val idToken = account.idToken // El token de Google para la autenticación
            Log.d(TAG, "handleSignInResult: ID Token: ${if (idToken != null) "Present" else "NULL"}")
            // Aquí puedes enviar el idToken a tu backend para la verificación

            Toast.makeText(this, "Login exitoso con Google", Toast.LENGTH_SHORT).show()
            // Ahora guarda el usuario y token de Google
            saveGoogleUserData(account)

        } catch (e: ApiException) {
            Log.e(TAG, "handleSignInResult: ApiException occurred", e)
            Log.e(TAG, "handleSignInResult: Status code: ${e.statusCode}")
            Log.e(TAG, "handleSignInResult: Status message: ${e.status}")

            val errorMessage = when (e.statusCode) {
                12501 -> "Login cancelado por el usuario"
                12502 -> "Error de red - Sin conexión"
                12500 -> "Error interno de Google Sign-In"
                7 -> "Error de red - Timeout"
                10 -> "Error de configuración - Verifica las credenciales de Google"
                else -> "Error de Google Sign-In (${e.statusCode}): ${e.message}"
            }

            Log.e(TAG, "handleSignInResult: Showing error: $errorMessage")
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // Método para guardar los datos del usuario de Google
    private fun saveGoogleUserData(account: GoogleSignInAccount) {
        Log.d(TAG, "saveGoogleUserData: Saving Google user data")
        Log.d(TAG, "saveGoogleUserData: Given name: ${account.givenName}")
        Log.d(TAG, "saveGoogleUserData: Family name: ${account.familyName}")
        Log.d(TAG, "saveGoogleUserData: Email: ${account.email}")
        Log.d(TAG, "saveGoogleUserData: Photo URL: ${account.photoUrl}")

        // Enviar datos de Google al backend
        performGoogleLogin(account)
    }

    private fun performGoogleLogin(account: GoogleSignInAccount) {
        Log.d(TAG, "performGoogleLogin: Starting Google login for email: ${account.email}")
        
        // Crear el body con los datos de Google que necesita el backend
        // PROBAR AMBOS FORMATOS: google_id vs idToken
        val googleToken = mapOf(
            "email" to (account.email ?: ""),
            "name" to (account.givenName ?: ""),
            "lastName" to (account.familyName ?: ""),
            "avatar" to (account.photoUrl?.toString() ?: ""),
            // FORMATO 1: Usar google_id (como en los ejemplos de Postman)
            "google_id" to (account.id ?: ""),
            // FORMATO 2: Usar idToken (como está actualmente)
            "idToken" to (account.idToken ?: "")
        )
        
        // Log detallado para debug
        Log.d(TAG, "performGoogleLogin: Sending data to backend:")
        Log.d(TAG, "performGoogleLogin: Email: ${account.email}")
        Log.d(TAG, "performGoogleLogin: Name: ${account.givenName}")
        Log.d(TAG, "performGoogleLogin: LastName: ${account.familyName}")
        Log.d(TAG, "performGoogleLogin: Google ID: ${account.id}")
        Log.d(TAG, "performGoogleLogin: ID Token: ${if (account.idToken != null) "Present (${account.idToken?.length} chars)" else "NULL"}")
        Log.d(TAG, "performGoogleLogin: Request body: $googleToken")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.googleLogin(googleToken)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d(TAG, "Google login successful for user: ${loginResponse.user.email}")
                        
                        // Guardar sesión del usuario
                        AuthManager.saveUserSession(loginResponse.user, loginResponse)
                        
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