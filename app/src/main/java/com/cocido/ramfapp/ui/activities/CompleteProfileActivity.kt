package com.cocido.ramfapp.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.ActivityCompleteProfileBinding
import com.cocido.ramfapp.models.CreateProfileRequest
import com.cocido.ramfapp.models.UpdateProfileRequest
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.ui.components.showErrorMessage
import com.cocido.ramfapp.ui.components.showInfoMessage
import com.cocido.ramfapp.ui.components.showSuccessMessage
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.launch

class CompleteProfileActivity : BaseActivity() {
    
    private lateinit var binding: ActivityCompleteProfileBinding
    private lateinit var currentUser: User
    
    override fun requiresAuthentication(): Boolean {
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupListeners()
        setupTextWatchers()
    }
    
    private fun setupUI() {
        // Configurar la UI básica
        binding.tvTitle.text = "Para continuar completa tu perfil"
        binding.tvSubtitle.text = "Por favor, proporciona la información adicional necesaria para completar tu perfil."
        
        // Obtener el usuario actual
        currentUser = AuthManager.getCurrentUser() ?: run {
            showErrorMessage("Error: Usuario no encontrado")
            finish()
            return
        }
        
        // Mostrar información del usuario
        binding.tvUserName.text = currentUser.getFullName()
        binding.tvUserEmail.text = currentUser.email
    }
    
    private fun setupListeners() {
        binding.btnCompleteProfile.setOnClickListener {
            completeProfile()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupTextWatchers() {
        binding.etDni.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilDni.error = null
            }
        })
        
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilPhone.error = null
            }
        })
        
        binding.etEnterpriseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilEnterpriseName.error = null
            }
        })
        
        binding.etJobPosition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilJobPosition.error = null
            }
        })
    }
    
    private fun completeProfile() {
        if (!validateForm()) {
            return
        }
        
        val profileData = CreateProfileRequest(
            dni = binding.etDni.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            enterpriseName = binding.etEnterpriseName.text.toString().trim(),
            jobPosition = binding.etJobPosition.text.toString().trim()
        )
        
        Log.d(TAG, "Creating profile with data:")
        Log.d(TAG, "  DNI: ${profileData.dni}")
        Log.d(TAG, "  Phone: ${profileData.phone}")
        Log.d(TAG, "  Enterprise: ${profileData.enterpriseName}")
        Log.d(TAG, "  Job Position: ${profileData.jobPosition}")
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val token = AuthManager.getAccessToken()
                if (token == null) {
                    showError("Error: Token de acceso no disponible")
                    return@launch
                }
                
                // Verificar si el usuario ya tiene perfil
                val currentUser = AuthManager.getCurrentUser()
                if (currentUser?.hasProfile() == true) {
                    Log.d(TAG, "User already has profile, redirecting to UserProfileActivity")
                    showInfoMessage("Ya tienes un perfil completado")
                    val intent = android.content.Intent(this@CompleteProfileActivity, UserProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@launch
                }
                
                // Usar el endpoint de actualización que es más robusto
                val updateRequest = UpdateProfileRequest(
                    name = currentUser?.name ?: "",
                    lastName = currentUser?.lastName ?: "",
                    email = currentUser?.email ?: "",
                    phone = profileData.phone,
                    dni = profileData.dni,
                    jobPosition = profileData.jobPosition,
                    company = profileData.enterpriseName
                )
                val response = RetrofitClient.profileService.updateProfile("Bearer $token", updateRequest)
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Profile created successfully")
                    showSuccessMessage("Perfil completado exitosamente")
                    
                    // Redirigir a la actividad de perfil
                    val intent = android.content.Intent(this@CompleteProfileActivity, UserProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "Profile creation failed: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                    
                    val errorMessage = when (response.code()) {
                        400 -> {
                            // Verificar si ya existe un perfil
                            if (response.errorBody()?.string()?.contains("already exists") == true) {
                                "Ya existe un perfil para este usuario"
                            } else {
                                "Datos inválidos. Verifique que todos los campos estén correctos."
                            }
                        }
                        401 -> "Sesión expirada. Por favor, inicie sesión nuevamente."
                        409 -> "Ya existe un perfil para este usuario"
                        422 -> "Datos inválidos. Verifique que todos los campos estén correctos."
                        else -> "Error del servidor: ${response.code()}. Intente nuevamente."
                    }
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating profile: ${e.message}", e)
                showError("Error de conexión: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validar DNI
        val dni = binding.etDni.text.toString().trim()
        if (dni.isEmpty()) {
            binding.tilDni.error = "El DNI es requerido"
            isValid = false
        } else if (!dni.matches(Regex("^[0-9]+$"))) {
            binding.tilDni.error = "El DNI debe contener solo números"
            isValid = false
        } else if (dni.length < 7) {
            binding.tilDni.error = "El DNI debe tener al menos 7 dígitos"
            isValid = false
        }
        
        // Validar teléfono
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilPhone.error = "El teléfono es requerido"
            isValid = false
        } else if (phone.length < 7) {
            binding.tilPhone.error = "El teléfono debe tener al menos 7 caracteres"
            isValid = false
        }
        
        // Validar empresa
        val enterpriseName = binding.etEnterpriseName.text.toString().trim()
        if (enterpriseName.isEmpty()) {
            binding.tilEnterpriseName.error = "El nombre de la empresa es requerido"
            isValid = false
        } else if (enterpriseName.length < 3) {
            binding.tilEnterpriseName.error = "El nombre de la empresa debe tener al menos 3 caracteres"
            isValid = false
        }
        
        // Validar puesto
        val jobPosition = binding.etJobPosition.text.toString().trim()
        if (jobPosition.isEmpty()) {
            binding.tilJobPosition.error = "La posición laboral es requerida"
            isValid = false
        } else if (jobPosition.length < 3) {
            binding.tilJobPosition.error = "La posición laboral debe tener al menos 3 caracteres"
            isValid = false
        }
        
        return isValid
    }
    
    private fun showLoading(show: Boolean) {
        binding.btnCompleteProfile.isEnabled = !show
        binding.btnCancel.isEnabled = !show
        
        if (show) {
            binding.btnCompleteProfile.text = "Completando perfil..."
        } else {
            binding.btnCompleteProfile.text = "Completar perfil"
        }
    }
    
    private fun showError(message: String) {
        showErrorMessage(message)
        Log.e(TAG, message)
    }
    
    companion object {
        private const val TAG = "CompleteProfileActivity"
    }
}
