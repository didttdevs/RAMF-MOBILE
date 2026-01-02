package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cocido.ramfapp.R
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.launch

/**
 * Activity principal para mostrar el perfil del usuario
 * Incluye navegación a edición de perfil y otras funcionalidades
 */
class UserProfileActivity : BaseActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var tvUserStatus: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserJobPosition: TextView
    private lateinit var tvUserLastLogin: TextView
    private lateinit var tvUserDni: TextView
    private lateinit var tvUserCompany: TextView
    private lateinit var btnChangePassword: com.google.android.material.button.MaterialButton


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
        tvUserStatus = findViewById(R.id.tvUserStatus)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserJobPosition = findViewById(R.id.tvUserJobPosition)
        tvUserLastLogin = findViewById(R.id.tvUserLastLogin)
        tvUserDni = findViewById(R.id.tvUserDni)
        tvUserCompany = findViewById(R.id.tvUserCompany)
        btnChangePassword = findViewById(R.id.btnChangePassword)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnChangePassword.setOnClickListener {
            openChangePassword()
        }
    }

    private fun loadUserData() {
        // Forzar actualización del usuario desde el servidor
        lifecycleScope.launch {
            try {
                AuthManager.fetchAndUpdateCurrentUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data: ${e.message}")
            }
            
            val user = AuthManager.getCurrentUser()
            user?.let {
                tvUserName.text = it.getFullName()
                tvUserEmail.text = it.email
                tvUserRole.text = it.roles?.firstOrNull()?.name?.replaceFirstChar { char -> char.uppercase() } ?: "Usuario"
                
                // Mostrar todos los campos del perfil
                tvUserStatus.text = if (it.isActive) "ACTIVO" else "INACTIVO"
            
                // Mostrar datos del perfil
                tvUserPhone.text = it.getPhone() ?: "No especificado"
                tvUserJobPosition.text = it.getJobPosition() ?: "No especificado"
                tvUserDni.text = it.getDni() ?: "No especificado"
                tvUserCompany.text = it.getCompany() ?: "No especificado"
                
                // Formatear último ingreso
                tvUserLastLogin.text = it.lastLogin?.let { loginDate ->
                    try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        val date = inputFormat.parse(loginDate)
                        "Último ingreso: ${outputFormat.format(date ?: java.util.Date())}"
                    } catch (e: Exception) {
                        "Último ingreso: ${loginDate}"
                    }
                } ?: "Último ingreso: No disponible"
                
                Glide.with(this@UserProfileActivity)
                    .load(it.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .into(profileImage)
            }
        }
    }
    
    
    private fun openChangePassword() {
        val dialog = com.cocido.ramfapp.ui.dialogs.ChangePasswordDialogFragment.newInstance()
        dialog.show(supportFragmentManager, "ChangePasswordDialog")
    }
    
    companion object {
        private const val TAG = "UserProfileActivity"
    }
}