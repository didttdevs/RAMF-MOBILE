package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cocido.ramfapp.R
import com.cocido.ramfapp.utils.AuthManager

/**
 * Activity principal para mostrar el perfil del usuario
 * Incluye navegación a edición de perfil y otras funcionalidades
 */
class UserProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnEditProfile: com.google.android.material.button.MaterialButton
    private lateinit var btnChangePassword: com.google.android.material.button.MaterialButton
    private lateinit var btnPrivacySettings: com.google.android.material.button.MaterialButton

    // Result launcher para cuando regrese de editar perfil
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Recargar datos del usuario
            loadUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        initViews()
        setupListeners()
        loadUserData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        profileImage = findViewById(R.id.profileImage)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnPrivacySettings = findViewById(R.id.btnPrivacySettings)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnEditProfile.setOnClickListener {
            openEditProfile()
        }
        
        btnChangePassword.setOnClickListener {
            openChangePassword()
        }
        
        btnPrivacySettings.setOnClickListener {
            openPrivacySettings()
        }
    }

    private fun loadUserData() {
        val user = AuthManager.getCurrentUser()
        user?.let {
            tvUserName.text = it.getFullName()
            tvUserEmail.text = it.email
            tvUserRole.text = it.role ?: "Usuario"
            // ID removed for security
            
            Log.d("UserProfileActivity", "Loading avatar for user: ${it.email}")
            Log.d("UserProfileActivity", "Avatar URL: ${it.avatar}")
            Log.d("UserProfileActivity", "Avatar is null or empty: ${it.avatar.isNullOrEmpty()}")
            
            Glide.with(this)
                .load(it.avatar)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(profileImage)
        } ?: run {
            Log.e("UserProfileActivity", "No user found in AuthManager")
        }
    }
    
    private fun openEditProfile() {
        val intent = Intent(this, ProfileEditActivity::class.java)
        editProfileLauncher.launch(intent)
    }
    
    private fun openChangePassword() {
        // Por ahora abrimos la edición de perfil, donde está el diálogo de cambio de contraseña
        openEditProfile()
    }
    
    private fun openPrivacySettings() {
        // TODO: Implementar configuración de privacidad
        android.widget.Toast.makeText(this, "Configuración de privacidad próximamente", android.widget.Toast.LENGTH_SHORT).show()
    }
}