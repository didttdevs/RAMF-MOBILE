package com.cocido.ramfapp.utils

import android.content.Context
import android.util.Log
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.ApiError
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

/**
 * Manejador de errores centralizado con mejores prácticas
 */
object ErrorHandler {
    private const val TAG = "ErrorHandler"
    
    /**
     * Procesar errores de red y API de forma consistente
     */
    fun handleError(throwable: Throwable, @Suppress("UNUSED_PARAMETER") context: Context): ErrorInfo {
        Log.e(TAG, "Handling error", throwable)
        
        return when (throwable) {
            is HttpException -> handleHttpError(throwable, context)
            is UnknownHostException -> ErrorInfo(
                title = "Sin conexión",
                message = "Verifica tu conexión a internet",
                type = ErrorType.NETWORK,
                action = ErrorAction.RETRY
            )
            is IOException -> ErrorInfo(
                title = "Error de conexión",
                message = "No se pudo conectar con el servidor",
                type = ErrorType.NETWORK,
                action = ErrorAction.RETRY
            )
            else -> ErrorInfo(
                title = "Error inesperado",
                message = throwable.message ?: "Ha ocurrido un error inesperado",
                type = ErrorType.UNKNOWN,
                action = ErrorAction.NONE
            )
        }
    }
    
    /**
     * Manejar errores HTTP específicos
     */
    private fun handleHttpError(exception: HttpException, @Suppress("UNUSED_PARAMETER") context: Context): ErrorInfo {
        val code = exception.code()
        val message = exception.message()
        
        return when (code) {
            400 -> ErrorInfo(
                title = "Solicitud inválida",
                message = "Los datos enviados no son válidos",
                type = ErrorType.CLIENT,
                action = ErrorAction.NONE
            )
            401 -> ErrorInfo(
                title = "No autorizado",
                message = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente",
                type = ErrorType.AUTH,
                action = ErrorAction.LOGIN
            )
            403 -> ErrorInfo(
                title = "Acceso denegado",
                message = "No tienes permisos para realizar esta acción",
                type = ErrorType.AUTH,
                action = ErrorAction.NONE
            )
            404 -> ErrorInfo(
                title = "No encontrado",
                message = "El recurso solicitado no existe",
                type = ErrorType.CLIENT,
                action = ErrorAction.NONE
            )
            408 -> ErrorInfo(
                title = "Tiempo agotado",
                message = "La solicitud tardó demasiado tiempo",
                type = ErrorType.NETWORK,
                action = ErrorAction.RETRY
            )
            429 -> ErrorInfo(
                title = "Demasiadas solicitudes",
                message = "Has realizado demasiadas solicitudes. Intenta más tarde",
                type = ErrorType.RATE_LIMIT,
                action = ErrorAction.RETRY_LATER
            )
            500 -> ErrorInfo(
                title = "Error del servidor",
                message = "El servidor está experimentando problemas. Intenta más tarde",
                type = ErrorType.SERVER,
                action = ErrorAction.RETRY_LATER
            )
            502, 503, 504 -> ErrorInfo(
                title = "Servicio no disponible",
                message = "El servicio está temporalmente no disponible",
                type = ErrorType.SERVER,
                action = ErrorAction.RETRY_LATER
            )
            else -> ErrorInfo(
                title = "Error HTTP $code",
                message = message ?: "Error de servidor desconocido",
                type = ErrorType.SERVER,
                action = ErrorAction.RETRY
            )
        }
    }
    
    /**
     * Procesar errores de la API personalizada
     */
    fun handleApiError(apiError: ApiError, @Suppress("UNUSED_PARAMETER") context: Context): ErrorInfo {
        return when (apiError.code.lowercase()) {
            "invalid_credentials" -> ErrorInfo(
                title = "Credenciales incorrectas",
                message = "El email o contraseña son incorrectos",
                type = ErrorType.AUTH,
                action = ErrorAction.NONE
            )
            "account_disabled" -> ErrorInfo(
                title = "Cuenta deshabilitada",
                message = "Tu cuenta ha sido deshabilitada. Contacta al administrador",
                type = ErrorType.AUTH,
                action = ErrorAction.NONE
            )
            "token_expired" -> ErrorInfo(
                title = "Sesión expirada",
                message = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente",
                type = ErrorType.AUTH,
                action = ErrorAction.LOGIN
            )
            "validation_error" -> ErrorInfo(
                title = "Error de validación",
                message = apiError.message,
                type = ErrorType.CLIENT,
                action = ErrorAction.NONE
            )
            "rate_limit_exceeded" -> ErrorInfo(
                title = "Límite excedido",
                message = "Has realizado demasiadas solicitudes. Intenta en unos minutos",
                type = ErrorType.RATE_LIMIT,
                action = ErrorAction.RETRY_LATER
            )
            else -> ErrorInfo(
                title = "Error de API",
                message = apiError.message,
                type = ErrorType.SERVER,
                action = ErrorAction.RETRY
            )
        }
    }
}

/**
 * Información de error estructurada
 */
data class ErrorInfo(
    val title: String,
    val message: String,
    val type: ErrorType,
    val action: ErrorAction,
    val details: Map<String, Any>? = null
)

/**
 * Tipos de error
 */
enum class ErrorType {
    NETWORK,    // Problemas de conexión
    AUTH,       // Problemas de autenticación/autorización
    CLIENT,     // Errores del cliente (400-499)
    SERVER,     // Errores del servidor (500-599)
    RATE_LIMIT, // Límite de velocidad excedido
    UNKNOWN     // Error desconocido
}

/**
 * Acciones recomendadas para el usuario
 */
enum class ErrorAction {
    RETRY,          // Reintentar la operación
    RETRY_LATER,    // Reintentar más tarde
    LOGIN,          // Iniciar sesión
    NONE            // No hay acción específica
}
