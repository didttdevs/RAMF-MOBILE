package com.cocido.ramfapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityProfileEditBinding
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.utils.ImageUtils
import com.cocido.ramfapp.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Activity para editar el perfil del usuario
 * Incluye funcionalidades completas de edición de datos personales,
 * cambio de avatar, y gestión de contraseñas
 */
class ProfileEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var viewModel: ProfileViewModel
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
            currentPhotoUri?.let { uri ->
                startImageCrop(uri)
            }
        }
    }
    
    // private val cropLauncher = registerForActivityResult(
    //     ActivityResultContracts.StartActivityForResult()
    // ) { result ->
    //     if (result.resultCode == Activity.RESULT_OK) {
    //         val resultUri = UCrop.getOutput(result.data!!)
    //         resultUri?.let { 
    //             updateAvatar(it)
    //         }
    //     } else if (result.resultCode == UCrop.RESULT_ERROR) {
    //         val cropError = UCrop.getError(result.data!!)
    //         showError("Error al recortar imagen: ${cropError?.message}")
    //     }
    // }
    
    private var currentPhotoUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        setupListeners()
        loadUserData()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }
    
    private fun setupUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Editar Perfil"
        }
        
        // Configurar validaciones en tiempo real
        setupTextWatchers()
    }
    
    private fun setupListeners() {
        binding.apply {
            // Avatar click
            btnChangeAvatar.setOnClickListener {
                showImageSourceDialog()
            }
            
            // Save button
            btnSave.setOnClickListener {
                saveProfile()
            }
            
            // Change password button
            btnChangePassword.setOnClickListener {
                showChangePasswordDialog()
            }
            
            // Delete account button removed
        }
    }
    
    private fun loadUserData() {
        val user = AuthManager.getCurrentUser()
        if (user != null) {
            currentUser = user
            populateFields()
        } else {
            showError("No se pudo cargar la información del usuario")
            finish()
        }
    }
    
    private fun populateFields() {
        binding.apply {
            // Información personal
            etFirstName.setText(currentUser.firstName ?: "")
            etLastName.setText(currentUser.lastName ?: "")
            etEmail.setText(currentUser.email)
            
            // Avatar
            if (!currentUser.avatar.isNullOrBlank()) {
                Glide.with(this@ProfileEditActivity)
                    .load(currentUser.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(ivAvatar)
            }
            
            // Información adicional
            tvUserRole.text = currentUser.role ?: "Usuario"
            tvCreatedAt.text = "Miembro desde: ${formatDate(currentUser.createdAt)}"
        }
    }
    
    private fun setupTextWatchers() {
        binding.apply {
            etFirstName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateFirstName()
                }
            })
            
            etLastName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateLastName()
                }
            })
        }
    }
    
    private fun validateFirstName(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        return when {
            firstName.isEmpty() -> {
                binding.tilFirstName.error = "El nombre es requerido"
                false
            }
            firstName.length < 2 -> {
                binding.tilFirstName.error = "El nombre debe tener al menos 2 caracteres"
                false
            }
            else -> {
                binding.tilFirstName.error = null
                true
            }
        }
    }
    
    private fun validateLastName(): Boolean {
        val lastName = binding.etLastName.text.toString().trim()
        return when {
            lastName.isEmpty() -> {
                binding.tilLastName.error = "El apellido es requerido"
                false
            }
            lastName.length < 2 -> {
                binding.tilLastName.error = "El apellido debe tener al menos 2 caracteres"
                false
            }
            else -> {
                binding.tilLastName.error = null
                true
            }
        }
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
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            startCamera()
        }
    }
    
    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }
    
    private fun startCamera() {
        val photoFile = File(cacheDir, "temp_photo.jpg")
        val uri = Uri.fromFile(photoFile)
        currentPhotoUri = uri
        cameraLauncher.launch(uri)
    }
    
    private fun startImageCrop(sourceUri: Uri) {
        com.cocido.ramfapp.ui.activities.ImageCropActivity.startForResult(this, sourceUri, CROP_IMAGE_REQUEST)
    }
    
    private fun updateAvatar(uri: Uri) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            try {
                val result = viewModel.updateAvatar(uri)
                if (result.isSuccess) {
                    // Actualizar UI
                    Glide.with(this@ProfileEditActivity)
                        .load(uri)
                        .circleCrop()
                        .into(binding.ivAvatar)
                    
                    Toast.makeText(this@ProfileEditActivity, "Avatar actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    showError("Error al actualizar avatar: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError("Error al actualizar avatar: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun saveProfile() {
        if (!validateForm()) {
            return
        }
        
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            
            try {
                val updatedUser = currentUser.copy(
                    firstName = binding.etFirstName.text.toString().trim(),
                    lastName = binding.etLastName.text.toString().trim()
                )
                
                val result = viewModel.updateProfile(updatedUser)
                if (result.isSuccess) {
                    Toast.makeText(this@ProfileEditActivity, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showError("Error al actualizar perfil: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError("Error al actualizar perfil: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        if (!validateFirstName()) isValid = false
        if (!validateLastName()) isValid = false
        
        return isValid
    }
    
    private fun showChangePasswordDialog() {
        val dialog = com.cocido.ramfapp.ui.dialogs.ChangePasswordDialogFragment()
        dialog.setOnPasswordChangedListener {
            Toast.makeText(this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
        }
        dialog.show(supportFragmentManager, "ChangePasswordDialog")
    }
    
    // Delete account functionality removed
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                showError(error)
                viewModel.clearError()
            }
        }
    }
    
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Fecha no disponible"
        
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            "Fecha no disponible"
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                val croppedUri = data?.getParcelableExtra<Uri>(com.cocido.ramfapp.ui.activities.ImageCropActivity.EXTRA_CROPPED_URI)
                if (croppedUri != null) {
                    updateAvatar(croppedUri)
                }
            }
        }
    }
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val CROP_IMAGE_REQUEST = 101
    }
}