package com.cocido.ramfapp.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.Profile
import com.cocido.ramfapp.models.UpdateProfileRequest
import com.cocido.ramfapp.models.ChangePasswordRequest
import com.cocido.ramfapp.network.AuthService
import com.cocido.ramfapp.network.ProfileService
import com.cocido.ramfapp.network.UserService
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
class ProfileRepository(private val context: Context) {
    
    private val authService: AuthService = RetrofitClient.authService
    private val profileService: ProfileService = RetrofitClient.profileService
    private val userService: UserService = RetrofitClient.userService
    
    /**
     * Obtener usuario actual
     */
    suspend fun getCurrentUser(): User = withContext(Dispatchers.IO) {
        try {
            // El token se agrega automáticamente por el interceptor de RetrofitClient
            val response = authService.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()
                Log.d("ProfileRepository", "API Response: $user")
                if (user != null) {
                    Log.d("ProfileRepository", "User data: name=${user.name}, email=${user.email}, phone=${user.getPhone()}, dni=${user.getDni()}, jobPosition=${user.getJobPosition()}, company=${user.getCompany()}")
                    Log.d("ProfileRepository", "Profile data: ${user.profile}")
                    user
                } else {
                    throw Exception("No se recibieron datos del usuario")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val isHtmlError = errorBody?.contains("<html>", ignoreCase = true) == true ||
                                  response.errorBody()?.contentType()?.toString()?.contains("text/html") == true
                
                Log.e("ProfileRepository", "Error response: ${response.code()} - ${response.message()}")
                Log.e("ProfileRepository", "Error body: $errorBody")
                
                if (isHtmlError && response.code() == 400) {
                    Log.e("ProfileRepository", "Received HTML error from Nginx on /auth/me - likely token validation issue")
                    throw Exception("Error de autenticación. El token no es válido. Por favor, inicia sesión nuevamente.")
                } else {
                    throw Exception("Error al obtener usuario: ${response.code()} - ${response.message()}")
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Actualizar información del perfil del usuario
     */
    suspend fun updateProfile(updateRequest: UpdateProfileRequest): Unit = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            val response = profileService.updateProfile("Bearer $token", updateRequest)
            if (!response.isSuccessful) {
                throw Exception("Error al actualizar perfil: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    
    /**
     * Cambiar contraseña del usuario
     * Usa el endpoint /auth/change-password que solo requiere autenticación (no permiso específico)
     */
    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): Unit = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            // Usar /auth/change-password en vez de /users/change-password
            // El endpoint /auth/change-password solo requiere @Auth(), no permisos específicos
            // Retorna 204 No Content
            // El token se agrega automáticamente por el interceptor de RetrofitClient
            val response = authService.changePassword(changePasswordRequest)
            
            if (response.code() != 204) {
                // Leer el cuerpo del error para obtener el mensaje exacto del backend
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepository", "Change password failed: ${response.code()}")
                Log.e("ProfileRepository", "Error body: $errorBody")
                
                // Verificar si el error viene en formato HTML (Nginx) o JSON (Backend)
                val isHtmlError = errorBody?.contains("<html>", ignoreCase = true) == true || 
                                  errorBody?.contains("Bad Request", ignoreCase = true) == true ||
                                  response.errorBody()?.contentType()?.toString()?.contains("text/html") == true
                
                // Determinar el mensaje de error específico
                val errorMessage = when (response.code()) {
                    400 -> {
                        if (isHtmlError) {
                            // Si es HTML, es un error de Nginx (problema con el token o formato de petición)
                            Log.e("ProfileRepository", "Received HTML error from Nginx - likely token or request format issue")
                            "Error de autenticación. Tu sesión puede haber expirado. Por favor, cierra sesión e inicia sesión nuevamente."
                        } else {
                            // Intentar parsear JSON si es posible
                            try {
                                val jsonError = errorBody?.let {
                                    // Si el error body es JSON, intentar extraer el mensaje
                                    if (it.contains("\"message\"") || it.contains("\"error\"")) {
                                        // Es JSON con mensaje de error
                                        when {
                                            it.contains("Password not match", ignoreCase = true) -> 
                                                "La contraseña actual es incorrecta"
                                            it.contains("must be different", ignoreCase = true) || 
                                            it.contains("cannot be the same", ignoreCase = true) -> 
                                                "La nueva contraseña debe ser diferente a la actual"
                                            it.contains("strong", ignoreCase = true) || 
                                            it.contains("uppercase", ignoreCase = true) || 
                                            it.contains("lowercase", ignoreCase = true) -> 
                                                "La contraseña debe contener al menos 8 caracteres, una mayúscula, una minúscula y un número"
                                            else -> null
                                        }
                                    } else null
                                }
                                jsonError ?: "Error de validación. Verifica que la contraseña actual sea correcta y que la nueva contraseña cumpla los requisitos."
                            } catch (e: Exception) {
                                "Error de validación. Verifica que la contraseña actual sea correcta y que la nueva contraseña cumpla los requisitos."
                            }
                        }
                    }
                    401 -> "Sesión expirada. Por favor, inicia sesión nuevamente"
                    403 -> "No tienes permisos para realizar esta acción"
                    else -> {
                        if (isHtmlError) {
                            "Error del servidor. Intenta más tarde o reinicia la sesión."
                        } else {
                            "Error al cambiar contraseña: ${response.code()} - ${errorBody ?: response.message()}"
                        }
                    }
                }
                
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Cambiar avatar del usuario
     */
    suspend fun changeAvatar(avatarFile: File): String = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            // Crear MultipartBody.Part para el archivo
            val requestFile = avatarFile.asRequestBody("image/*".toMediaType())
            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                avatarFile.name,
                requestFile
            )
            
            val response = userService.changeAvatar("Bearer $token", avatarPart)
            if (response.isSuccessful) {
                val result = response.body()
                result?.get("avatar_url") ?: result?.get("secure_url") ?: throw Exception("No se recibió URL del avatar")
            } else {
                throw Exception("Error al cambiar avatar: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Eliminar cuenta del usuario
     */
    suspend fun deleteAccount(): Unit = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            val userId = AuthManager.getCurrentUser()?.id
            if (userId == null) {
                throw Exception("ID de usuario no disponible")
            }
            
            val response = userService.deleteAccount("Bearer $token", userId)
            if (!response.isSuccessful) {
                throw Exception("Error al eliminar cuenta: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
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
            
            // El token se agrega automáticamente por el interceptor de RetrofitClient
            val response = authService.getCurrentUser()
            
            when (response.code()) {
                200 -> {
                    val user = response.body()
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
    
    /**
     * Actualizar avatar del usuario
     */
    suspend fun updateAvatar(avatarUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            // Convertir URI a File
            val avatarFile = File(avatarUri.path ?: "")
            val requestFile = avatarFile.asRequestBody("image/*".toMediaType())
            val avatarPart = MultipartBody.Part.createFormData("avatar", avatarFile.name, requestFile)
            
            val response = profileService.updateAvatar("Bearer $token", avatarPart)
            if (response.isSuccessful) {
                // Asumir que la respuesta contiene la URL del avatar
                response.body()?.toString() ?: throw Exception("No se recibió URL del avatar")
            } else {
                throw Exception("Error al actualizar avatar: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Eliminar avatar del usuario
     */
    suspend fun removeAvatar(): Unit = withContext(Dispatchers.IO) {
        try {
            val token = AuthManager.getAccessToken()
            if (token == null) {
                throw Exception("Token de acceso no disponible")
            }
            
            val response = profileService.removeAvatar("Bearer $token")
            if (!response.isSuccessful) {
                throw Exception("Error al eliminar avatar: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Método de prueba para verificar el token
     */
    suspend fun testToken(): Result<String> {
        return try {
            val response = profileService.testToken()
            if (response.isSuccessful) {
                val message = response.body() ?: "Token válido"
                Log.d("ProfileRepository", "Test token response: $message")
                Result.success(message)
            } else {
                Log.e("ProfileRepository", "Test token failed: ${response.code()} - ${response.message()}")
                throw Exception("Error al verificar token: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Test token error: ${e.message}")
            Result.failure(e)
        }
    }
}
