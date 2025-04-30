package com.example.rafapp.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rafapp.R
import com.example.rafapp.models.LoginRequest
import com.example.rafapp.models.LoginResponse
import com.example.rafapp.models.User
import com.example.rafapp.network.RetrofitClient
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

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: FrameLayout
    private lateinit var sharedPref: SharedPreferences
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    // Para el código de resultado de la actividad de Google
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)

        // Configura el cliente de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
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
    }

    private fun performLogin(email: String, password: String) {
        val authService = RetrofitClient.authService
        val loginRequest = LoginRequest(email, password)

        authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.token
                    val user = response.body()?.user

                    if (!token.isNullOrEmpty() && user != null) {
                        saveUserData(user, token)
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Error: Token vacío", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error en login: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error en login: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Método para manejar el resultado del login con Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si el código de la solicitud es el que esperamos (RC_SIGN_IN)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
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
            Toast.makeText(this, "Error de Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para guardar los datos del usuario de Google
    private fun saveGoogleUserData(account: GoogleSignInAccount) {
        val user = User(
            firstName = account.givenName ?: "",
            lastName = account.familyName ?: "",
            email = account.email ?: "",
            avatar = account.photoUrl?.toString() ?: "",
            role = "user"  // Valor por defecto para `role`
        )
        val token = account.idToken ?: ""

        saveUserData(user, token)
        goToMainActivity()
    }



    private fun saveUserData(user: User, token: String) {
        val userJson = Gson().toJson(user)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            putString("user_data", userJson)
            apply()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
