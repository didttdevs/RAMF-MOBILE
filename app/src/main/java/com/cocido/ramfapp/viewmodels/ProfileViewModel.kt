package com.cocido.ramfapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.UpdateProfileRequest
import com.cocido.ramfapp.models.ChangePasswordRequest
import com.cocido.ramfapp.repository.ProfileRepository
import com.cocido.ramfapp.utils.Resource
import android.content.Context
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el perfil del usuario
 * Basado en la funcionalidad de la página web
 */
class ProfileViewModel(private val context: Context) : ViewModel() {
    
    private val profileRepository = ProfileRepository(context)
    
    // Estados del perfil
    private val _profileState = MutableLiveData<Resource<User>>()
    val profileState: LiveData<Resource<User>> = _profileState
    
    // Estados de actualización
    private val _updateState = MutableLiveData<Resource<Unit>>()
    val updateState: LiveData<Resource<Unit>> = _updateState
    
    // Estados de cambio de contraseña
    private val _changePasswordState = MutableLiveData<Resource<Unit>>()
    val changePasswordState: LiveData<Resource<Unit>> = _changePasswordState
    
    // Estados de cambio de avatar
    private val _changeAvatarState = MutableLiveData<Resource<String>>()
    val changeAvatarState: LiveData<Resource<String>> = _changeAvatarState
    
    // Estados de actualización de avatar (para el diálogo)
    private val _avatarUpdateState = MutableLiveData<Resource<String>>()
    val avatarUpdateState: LiveData<Resource<String>> = _avatarUpdateState
    
    // Estados de eliminación de cuenta
    private val _deleteAccountState = MutableLiveData<Resource<Unit>>()
    val deleteAccountState: LiveData<Resource<Unit>> = _deleteAccountState
    
    init {
        loadProfile()
    }
    
    /**
     * Cargar perfil del usuario
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            try {
                val user = profileRepository.getCurrentUser()
                _profileState.value = Resource.Success(user)
            } catch (e: Exception) {
                _profileState.value = Resource.Error(e.message ?: "Error al cargar el usuario")
            }
        }
    }
    
    /**
     * Actualizar perfil del usuario
     */
    fun updateProfile(updateRequest: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            try {
                profileRepository.updateProfile(updateRequest)
                _updateState.value = Resource.Success(Unit)
                // Recargar perfil después de actualizar
                loadProfile()
            } catch (e: Exception) {
                _updateState.value = Resource.Error(e.message ?: "Error al actualizar el perfil")
            }
        }
    }
    
    /**
     * Cambiar contraseña del usuario
     */
    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading()
            try {
                profileRepository.changePassword(changePasswordRequest)
                _changePasswordState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _changePasswordState.value = Resource.Error(e.message ?: "Error al cambiar la contraseña")
            }
        }
    }
    
    /**
     * Cambiar avatar del usuario
     */
    fun changeAvatar(avatarFile: java.io.File) {
        viewModelScope.launch {
            _changeAvatarState.value = Resource.Loading()
            try {
                val avatarUrl = profileRepository.changeAvatar(avatarFile)
                _changeAvatarState.value = Resource.Success(avatarUrl)
                // Recargar perfil después de cambiar avatar
                loadProfile()
            } catch (e: Exception) {
                _changeAvatarState.value = Resource.Error(e.message ?: "Error al cambiar el avatar")
            }
        }
    }
    
    /**
     * Actualizar avatar del usuario
     */
    fun updateAvatar(avatarUri: android.net.Uri) {
        viewModelScope.launch {
            _avatarUpdateState.value = Resource.Loading()
            try {
                val avatarUrl = profileRepository.updateAvatar(avatarUri)
                _avatarUpdateState.value = Resource.Success(avatarUrl)
                // Recargar perfil después de cambiar avatar
                loadProfile()
            } catch (e: Exception) {
                _avatarUpdateState.value = Resource.Error(e.message ?: "Error al actualizar el avatar")
            }
        }
    }
    
    /**
     * Eliminar avatar del usuario
     */
    fun removeAvatar() {
        viewModelScope.launch {
            _avatarUpdateState.value = Resource.Loading()
            try {
                profileRepository.removeAvatar()
                _avatarUpdateState.value = Resource.Success("")
                // Recargar perfil después de eliminar avatar
                loadProfile()
            } catch (e: Exception) {
                _avatarUpdateState.value = Resource.Error(e.message ?: "Error al eliminar el avatar")
            }
        }
    }
    
    /**
     * Eliminar cuenta del usuario
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _deleteAccountState.value = Resource.Loading()
            try {
                profileRepository.deleteAccount()
                _deleteAccountState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _deleteAccountState.value = Resource.Error(e.message ?: "Error al eliminar la cuenta")
            }
    }
}

/**
     * Limpiar estados
     */
    fun clearStates() {
        _updateState.value = null
        _changePasswordState.value = null
        _changeAvatarState.value = null
        _deleteAccountState.value = null
    }
}