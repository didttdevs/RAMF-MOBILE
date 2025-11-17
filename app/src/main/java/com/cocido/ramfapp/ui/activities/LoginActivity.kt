package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import androidx.activity.result.contract.ActivityResultContracts

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.LoginRequest
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.CreateProfileRequest
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.ui.components.showErrorMessage
import com.cocido.ramfapp.ui.components.showInfoMessage
import com.cocido.ramfapp.ui.components.showSuccessMessage
import com.cocido.ramfapp.ui.dialogs.LoginSettingsDialogFragment
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
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USE_V2_LAYOUT = "use_v2_layout"
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
    private lateinit var forgotPasswordLink: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences
    private var isUsingV2Layout = true

    // Activity Result Launcher para Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Cargar preferencia de layout
        isUsingV2Layout = sharedPreferences.getBoolean(KEY_USE_V2_LAYOUT, true)
        
        // Cargar el layout según la preferencia
        loadLayout()
        
        AuthManager.initialize(this)
        
        // Verificar si viene de sesión expirada
        checkSessionExpired()

        initViews()

        // Configura el cliente de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupListeners()
    }
    
    private fun loadLayout() {
        if (isUsingV2Layout) {
            setContentView(R.layout.activity_login)
            // Cargar GIF animado de fondo solo para v2
            loadBackgroundGif()
        } else {
            setContentView(R.layout.activity_login_v1)
        }
    }
    
    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink)
        btnSettings = findViewById(R.id.btnSettings)
    }
    
    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showInfoMessage("Ingresá tu email y contraseña para continuar")
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

        forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        
        // Agrega el evento para cambiar layout
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun showSettingsDialog() {
        val dialog = LoginSettingsDialogFragment.newInstance(isUsingV2Layout) { selectedVersion ->
            // Aplicar el nuevo layout
            if (selectedVersion != isUsingV2Layout) {
                isUsingV2Layout = selectedVersion
                
                // Guardar preferencia
                sharedPreferences.edit()
                    .putBoolean(KEY_USE_V2_LAYOUT, isUsingV2Layout)
                    .apply()
                
                // Recargar activity con nuevo layout
                finish()
                startActivity(intent)
            }
        }
        dialog.show(supportFragmentManager, "LoginSettingsDialog")
    }
    
    private fun loadBackgroundGif() {
        try {
            val backgroundGif = findViewById<ImageView>(R.id.backgroundGif)
            Glide.with(this)
                .asGif()
                .load(R.drawable.anim_baniado)
                .override(1920, 1080)  // Forzar alta resolución
                .fitCenter()
                .into(backgroundGif)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading background GIF", e)
        }
    }
    
    private fun checkSessionExpired() {
        val sessionExpired = intent.getBooleanExtra("session_expired", false)
        val message = intent.getStringExtra("message")
        
        if (sessionExpired && !message.isNullOrEmpty()) {
            showInfoMessage(message)
        }
    }

    private fun performLogin(email: String, password: String) {
        
        val loginRequest = LoginRequest(email, password)

        // Usar corrutinas para el login
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.login(loginRequest)
                
                // El backend retorna 202 Accepted en vez de 200 OK
                if (response.code() == 202) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Validar que el token no esté vacío
                        if (loginResponse.accessToken.isBlank()) {
                            Log.e("LoginActivity", "Login response received but accessToken is empty")
                            showErrorMessage("Error: Token de acceso vacío")
                            return@launch
                        }
                        
                        Log.d("LoginActivity", "Login successful, token length: ${loginResponse.accessToken.length}")
                        Log.d("LoginActivity", "Token preview: ${loginResponse.accessToken.take(20)}...")
                        
                        // Guardar sesión del usuario
                        AuthManager.saveUserSession(loginResponse.user, loginResponse)
                        
                        // Verificar que el token se guardó correctamente
                        val savedToken = AuthManager.getAccessToken()
                        if (savedToken.isNullOrBlank()) {
                            Log.e("LoginActivity", "Token not saved correctly after login")
                            showErrorMessage("Error al guardar sesión")
                            return@launch
                        }

                        // Obtener datos completos del usuario desde /api/auth/me (opcional, no bloqueante)
                        lifecycleScope.launch {
                            try {
                                val freshUser = AuthManager.fetchAndUpdateCurrentUser()
                                if (freshUser != null) {
                                    Log.d("LoginActivity", "User data refreshed successfully")
                                }
                            } catch (e: Exception) {
                                Log.w("LoginActivity", "Failed to fetch user data, but login succeeded: ${e.message}")
                                // No bloquear el login si falla /auth/me
                            }
                        }

                        showSuccessMessage("Login exitoso")
                        goToMainActivity()
                    } else {
                        Log.e("LoginActivity", "Login response body is null")
                        showErrorMessage("Error en login: respuesta inválida")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 -> "Cuenta deshabilitada"
                        404 -> "Usuario no encontrado"
                        else -> "Error en login: ${response.code()} - ${response.message()}"
                    }
                    showErrorMessage(errorMessage)
                }
            } catch (e: Exception) {
                showErrorMessage("Error de conexión: ${e.message}")
            }
        }
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            showErrorMessage("Error al iniciar Google Sign-In: ${e.message}")
        }
    }

    // Método para manejar el resultado del login con Google
    private fun handleGoogleSignInResult(resultCode: Int, data: Intent?) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } catch (e: Exception) {
            showErrorMessage("Error procesando resultado de Google: ${e.message}")
        }
    }

    // Método para manejar el resultado de Google Sign-In
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // El inicio de sesión con Google fue exitoso, ahora puedes manejar la cuenta
            val idToken = account.idToken // El token de Google para la autenticación
            // Aquí puedes enviar el idToken a tu backend para la verificación

            showSuccessMessage("Login exitoso con Google")
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

            showErrorMessage(errorMessage)
        }
    }

    // Método para guardar los datos del usuario de Google
    private fun saveGoogleUserData(account: GoogleSignInAccount) {

        // Enviar datos de Google al backend
        performGoogleLogin(account)
    }

    private fun performGoogleLogin(account: GoogleSignInAccount) {
        Log.d(TAG, "performGoogleLogin: Starting Google login for email: ${account.email}")
        
        // El backend solo espera el idToken, extrae el resto del payload del token
        val idToken = account.idToken
        if (idToken.isNullOrEmpty()) {
            showErrorMessage("Error: No se obtuvo token de Google")
            return
        }

        // Enviar SOLO el idToken como espera el backend
        val googleToken = mapOf("idToken" to idToken)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.googleLogin(googleToken)
                
                // El backend retorna 202 Accepted en vez de 200 OK
                if (response.code() == 202) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d(TAG, "Google login successful for user: ${loginResponse.user.email}")
                        Log.d(TAG, "Backend returned user:")
                        Log.d(TAG, "  id: ${loginResponse.user.id}")
                        Log.d(TAG, "  email: ${loginResponse.user.email}")
                        Log.d(TAG, "  name: ${loginResponse.user.name}")
                        Log.d(TAG, "  lastName: ${loginResponse.user.lastName}")
                        Log.d(TAG, "  avatar: ${loginResponse.user.avatar}")

                        // Guardar sesión del usuario (el backend ya extrajo los datos del idToken)
                        AuthManager.saveUserSession(loginResponse.user, loginResponse)

                        // Obtener datos completos del usuario desde /api/auth/me
                        lifecycleScope.launch {
                            val freshUser = AuthManager.fetchAndUpdateCurrentUser()
                            if (freshUser != null) {
                                Log.d(TAG, "User data updated from /api/auth/me: ${freshUser.getFullName()}")
                            } else {
                                Log.w(TAG, "Could not fetch fresh user data, using login response data")
                            }
                        }

                        showSuccessMessage("Login exitoso con Google")
                        goToMainActivity()
                    } else {
                        Log.e(TAG, "Google login failed: Response body is null")
                        showErrorMessage("Error: Respuesta vacía del servidor")
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
                    showErrorMessage(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google login exception", e)
                showErrorMessage("Error de conexión: ${e.message}")
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