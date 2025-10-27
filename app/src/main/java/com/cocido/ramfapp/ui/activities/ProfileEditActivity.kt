package com.cocido.ramfapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityProfileEditBinding
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.UpdateProfileRequest
import com.cocido.ramfapp.repository.ProfileRepository
import com.cocido.ramfapp.utils.TokenManager
import com.cocido.ramfapp.utils.ImageUtils
import com.cocido.ramfapp.viewmodels.ProfileViewModel
import com.cocido.ramfapp.viewmodels.ProfileViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

/**
 * Activity completa para editar el perfil del usuario
 * Basada en la funcionalidad de la página web
 * Incluye: cambio de avatar, edición de datos, cambio de contraseña
 */
class ProfileEditActivity : BaseActivity() {
    
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileRepository: ProfileRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var currentUser: User
    
    // Image picker launchers
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { startImageCrop(it) }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result
        }
    }
    
    private val imageCropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedImageUri = result.data?.getParcelableExtra<Uri>("cropped_image_uri")
            croppedImageUri?.let { updateAvatar(it) }
        }
    }
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar componentes
        profileRepository = ProfileRepository(this)
        tokenManager = TokenManager(this)
        viewModel = ViewModelProvider(this, ProfileViewModelFactory(this))[ProfileViewModel::class.java]
        
        setupToolbar()
        setupListeners()
        setupTextWatchers()
        loadUserData()
        setupObservers()
    }
    
    private fun setupToolbar() {
        // Toolbar is now handled by the custom app bar in the layout
    }
    
    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
        
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        binding.btnChangeAvatar.setOnClickListener {
            showImageSourceDialog()
        }
        
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    
    private fun setupTextWatchers() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilName.error = null
            }
        })
        
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilEmail.error = null
            }
        })
        
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
        
        // Text watchers for other fields
        
        binding.etJobPosition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilJobPosition.error = null
            }
        })
    }
    
    private fun setupObservers() {
        viewModel.profileState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    showLoading(true)
                }
                        is com.cocido.ramfapp.utils.Resource.Success -> {
                            showLoading(false)
                            populateFieldsFromUser(state.data)
                        }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {}
            }
        }
        
        viewModel.updateState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    showLoading(true)
                }
                is com.cocido.ramfapp.utils.Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {}
            }
        }
        
        viewModel.changePasswordState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    showLoading(true)
                }
                is com.cocido.ramfapp.utils.Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {}
            }
        }
        
        viewModel.changeAvatarState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    showLoading(true)
                }
                is com.cocido.ramfapp.utils.Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Avatar actualizado exitosamente", Toast.LENGTH_SHORT).show()
                }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {}
            }
        }
        
        viewModel.deleteAccountState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    showLoading(true)
                }
                is com.cocido.ramfapp.utils.Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    // Navegar al login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {}
            }
        }
    }
    
    private fun loadUserData() {
        // Cargar perfil desde la API
        viewModel.loadProfile()
    }
    
    private fun populateFields() {
        // Este método se mantiene para compatibilidad
        // Los datos se cargan desde populateFieldsFromProfile
    }
    
    private fun populateFieldsFromUser(user: com.cocido.ramfapp.models.User) {
        binding.apply {
            // Información del usuario
            etName.setText(user.name)
            etLastName.setText(user.lastName)
            etEmail.setText(user.email)
            // Verificar si el usuario tiene perfil
            if (user.hasProfile()) {
                etPhone.setText(user.getPhone() ?: "")
                etDni.setText(user.getDni() ?: "")
                etJobPosition.setText(user.getJobPosition() ?: "")
                etCompany.setText(user.getCompany() ?: "")
            } else {
                // Usuario sin perfil - campos vacíos para completar
                etPhone.setText("")
                etDni.setText("")
                etJobPosition.setText("")
                etCompany.setText("")
            }
            
            // Cargar avatar si existe
            if (!user.avatar.isNullOrEmpty()) {
                Glide.with(this@ProfileEditActivity)
                    .load(user.avatar)
                    .transform(CircleCrop())
                    .into(binding.ivAvatar)
            }
        }
    }
    
    private fun saveProfile() {
        if (!validateForm()) return
        
        val name = binding.etName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val dni = binding.etDni.text.toString().trim()
        val jobPosition = binding.etJobPosition.text.toString().trim()
        val company = binding.etCompany.text.toString().trim()
        
        val profileData = UpdateProfileRequest(
            name = name,
            lastName = lastName,
            email = email,
            phone = phone,
            dni = dni,
            jobPosition = jobPosition,
            company = company
        )
        
        viewModel.updateProfile(profileData)
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validar nombre
        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.tilName.error = "El nombre es requerido"
            isValid = false
        }
        
        // Validar apellido
        if (binding.etLastName.text.toString().trim().isEmpty()) {
            binding.tilLastName.error = "El apellido es requerido"
            isValid = false
        }
        
        // Validar email
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.tilEmail.error = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email inválido"
            isValid = false
        }
        
        // Validar DNI
        val dni = binding.etDni.text.toString().trim()
        if (dni.isEmpty()) {
            binding.tilDni.error = "El DNI es requerido"
            isValid = false
        } else if (!dni.matches(Regex("^[0-9]+$"))) {
            binding.tilDni.error = "El DNI debe contener solo números"
            isValid = false
        }
        
        // Validar teléfono
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilPhone.error = "El teléfono es requerido"
            isValid = false
        } else if (phone.length < 8) {
            binding.tilPhone.error = "El teléfono debe tener al menos 8 caracteres"
            isValid = false
        }
        
        // Validación de empresa removida
        
        // Validar posición laboral
        val jobPosition = binding.etJobPosition.text.toString().trim()
        if (jobPosition.isEmpty()) {
            binding.tilJobPosition.error = "La posición laboral es requerida"
            isValid = false
        }
        
        return isValid
    }
    
    private fun showChangePasswordDialog() {
        // TODO: Implementar diálogo de cambio de contraseña
        Toast.makeText(this, "Cambio de contraseña próximamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun showImageSourceDialog() {
        val options = arrayOf("Cámara", "Galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            return
        }
        
        val photoFile = File.createTempFile("profile_", ".jpg", cacheDir)
        val photoUri = Uri.fromFile(photoFile)
        cameraLauncher.launch(photoUri)
    }
    
    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }
    
    private fun startImageCrop(imageUri: Uri) {
        val intent = Intent(this, ImageCropActivity::class.java).apply {
            putExtra("image_uri", imageUri.toString())
        }
        imageCropLauncher.launch(intent)
    }
    
    private fun updateAvatar(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                // Convertir URI a File
                val inputStream = contentResolver.openInputStream(imageUri)
                val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = file.outputStream()
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                // Llamar al ViewModel para cambiar avatar
                viewModel.changeAvatar(file)
                
                // Actualizar imagen con Glide
                Glide.with(this@ProfileEditActivity)
                    .load(imageUri)
                    .transform(CircleCrop())
                    .into(binding.ivAvatar)
            } catch (e: Exception) {
                showError("Error al procesar imagen: ${e.message}")
            }
        }
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cuenta")
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                confirmDeleteAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun confirmDeleteAccount() {
        // Llamar al ViewModel para eliminar cuenta
        viewModel.deleteAccount()
    }
    
    private fun showLoading(show: Boolean) {
        binding.btnSave.isEnabled = !show
        binding.btnSave.text = if (show) "Guardando..." else "Guardar Cambios"
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
