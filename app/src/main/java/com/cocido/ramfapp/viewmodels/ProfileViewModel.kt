package com.cocido.ramfapp.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.network.AuthService
import com.cocido.ramfapp.repository.ProfileRepository
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el perfil del usuario
 * Maneja la lógica de negocio para edición de perfil, cambio de avatar,
 * cambio de contraseña y eliminación de cuenta
 */
class ProfileViewModel : ViewModel() {
    
    private val profileRepository = ProfileRepository()
    
    // UI State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _profileUpdated = MutableLiveData<Boolean>()
    val profileUpdated: LiveData<Boolean> = _profileUpdated
    
    // Profile data state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    /**
     * Cargar información del usuario actual
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = AuthManager.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar información del usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualizar perfil del usuario
     */
    suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = profileRepository.updateProfile(user)
            
            if (result.isSuccess) {
                // Actualizar el usuario en AuthManager
                AuthManager.updateCurrentUser(user)
                _currentUser.value = user
                _profileUpdated.value = true
            }
            
            result
        } catch (e: Exception) {
            _errorMessage.value = "Error al actualizar perfil: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Actualizar avatar del usuario
     */
    suspend fun updateAvatar(avatarUri: Uri): Result<String> {
        return try {
            _isLoading.value = true
            
            // Comprimir y subir imagen
            val compressedUri = ImageUtils.compressImage(avatarUri)
            val result = profileRepository.updateAvatar(compressedUri)
            
            if (result.isSuccess) {
                val avatarUrl = result.getOrNull()
                val currentUser = _currentUser.value
                if (currentUser != null && avatarUrl != null) {
                    val updatedUser = currentUser.copy(avatar = avatarUrl)
                    AuthManager.updateCurrentUser(updatedUser)
                    _currentUser.value = updatedUser
                }
            }
            
            result
        } catch (e: Exception) {
            _errorMessage.value = "Error al actualizar avatar: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Cambiar contraseña del usuario
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = profileRepository.changePassword(currentPassword, newPassword)
            
            if (result.isFailure) {
                _errorMessage.value = "Error al cambiar contraseña: ${result.exceptionOrNull()?.message}"
            }
            
            result
        } catch (e: Exception) {
            _errorMessage.value = "Error al cambiar contraseña: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Eliminar cuenta del usuario
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            _isLoading.value = true
            
            val result = profileRepository.deleteAccount()
            
            if (result.isSuccess) {
                // Hacer logout después de eliminar la cuenta
                AuthManager.logout()
            } else {
                _errorMessage.value = "Error al eliminar cuenta: ${result.exceptionOrNull()?.message}"
            }
            
            result
        } catch (e: Exception) {
            _errorMessage.value = "Error al eliminar cuenta: ${e.message}"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Validar formulario de perfil
     */
    fun validateProfileForm(firstName: String, lastName: String): ProfileValidationResult {
        val errors = mutableListOf<String>()
        
        if (firstName.trim().isEmpty()) {
            errors.add("El nombre es requerido")
        } else if (firstName.trim().length < 2) {
            errors.add("El nombre debe tener al menos 2 caracteres")
        }
        
        if (lastName.trim().isEmpty()) {
            errors.add("El apellido es requerido")
        } else if (lastName.trim().length < 2) {
            errors.add("El apellido debe tener al menos 2 caracteres")
        }
        
        return ProfileValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validar formulario de cambio de contraseña
     */
    fun validatePasswordForm(currentPassword: String, newPassword: String, confirmPassword: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (currentPassword.isEmpty()) {
            errors.add("La contraseña actual es requerida")
        }
        
        if (newPassword.isEmpty()) {
            errors.add("La nueva contraseña es requerida")
        } else if (newPassword.length < 8) {
            errors.add("La nueva contraseña debe tener al menos 8 caracteres")
        } else if (!isValidPassword(newPassword)) {
            errors.add("La nueva contraseña debe contener al menos una letra mayúscula, una minúscula y un número")
        }
        
        if (confirmPassword.isEmpty()) {
            errors.add("La confirmación de contraseña es requerida")
        } else if (newPassword != confirmPassword) {
            errors.add("Las contraseñas no coinciden")
        }
        
        if (currentPassword == newPassword) {
            errors.add("La nueva contraseña debe ser diferente a la actual")
        }
        
        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validar si una contraseña cumple con los requisitos de seguridad
     */
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUpperCase && hasLowerCase && hasDigit
    }
    
    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Marcar perfil como no actualizado
     */
    fun markProfileAsNotUpdated() {
        _profileUpdated.value = false
    }
}

/**
 * Resultado de validación del formulario de perfil
 */
data class ProfileValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Resultado de validación del formulario de contraseña
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
