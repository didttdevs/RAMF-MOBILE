package com.cocido.ramfapp.repository

import android.net.Uri
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.network.AuthService
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repository para gestionar operaciones relacionadas con el perfil del usuario
 * Maneja la comunicación con la API para actualización de perfil, avatar y contraseña
 */
class ProfileRepository {
    
    private val authService: AuthService = RetrofitClient.authService
    
    /**
     * Actualizar información del perfil del usuario
     */
    suspend fun updateProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            val updateData = mapOf(
                "first_name" to (user.firstName ?: ""),
                "last_name" to (user.lastName ?: "")
            )
            
            val response = authService.updateProfile(
                token = "Bearer $token",
                profileData = updateData
            )
            
            when (response.code()) {
                200 -> Result.success(Unit)
                400 -> Result.failure(Exception("Datos de perfil inválidos"))
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                403 -> Result.failure(Exception("No tienes permisos para actualizar el perfil"))
                404 -> Result.failure(Exception("Usuario no encontrado"))
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar avatar del usuario
     */
    suspend fun updateAvatar(avatarUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            // Crear archivo temporal
            val file = File(avatarUri.path ?: "")
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Archivo de imagen no encontrado"))
            }
            
            // Preparar multipart request
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                file.name,
                requestFile
            )
            
            val response = authService.updateAvatar(
                token = "Bearer $token",
                avatar = avatarPart
            )
            
            when (response.code()) {
                200 -> {
                    val avatarUrl = response.body()?.data?.get("avatar_url") as? String
                    if (avatarUrl != null) {
                        Result.success(avatarUrl)
                    } else {
                        Result.failure(Exception("No se recibió URL del avatar"))
                    }
                }
                400 -> Result.failure(Exception("Formato de imagen inválido"))
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                413 -> Result.failure(Exception("La imagen es demasiado grande"))
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cambiar contraseña del usuario
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            val passwordData = mapOf(
                "current_password" to currentPassword,
                "new_password" to newPassword
            )
            
            val response = authService.changePassword(
                token = "Bearer $token",
                passwordData = passwordData
            )
            
            when (response.code()) {
                200 -> Result.success(Unit)
                400 -> Result.failure(Exception("Contraseña actual incorrecta"))
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                422 -> Result.failure(Exception("La nueva contraseña no cumple los requisitos"))
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar cuenta del usuario
     */
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            val response = authService.deleteAccount("Bearer $token")
            
            when (response.code()) {
                200 -> Result.success(Unit)
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                403 -> Result.failure(Exception("No tienes permisos para eliminar la cuenta"))
                404 -> Result.failure(Exception("Usuario no encontrado"))
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener información actualizada del perfil
     */
    suspend fun getCurrentProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            val response = authService.getCurrentUser("Bearer $token")
            
            when (response.code()) {
                200 -> {
                    val user = response.body()?.data
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("No se recibieron datos del usuario"))
                    }
                }
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                404 -> Result.failure(Exception("Usuario no encontrado"))
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validar si el email está disponible para cambio
     */
    suspend fun isEmailAvailable(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Token de acceso no disponible"))
            }
            
            val response = authService.checkEmailAvailability(
                token = "Bearer $token",
                email = email
            )
            
            when (response.code()) {
                200 -> Result.success(true)
                409 -> Result.success(false) // Email ya existe
                401 -> {
                    AuthManager.logout()
                    Result.failure(Exception("Sesión expirada"))
                }
                else -> Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
