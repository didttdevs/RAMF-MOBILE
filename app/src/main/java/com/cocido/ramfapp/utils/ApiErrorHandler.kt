package com.cocido.ramfapp.utils

import android.util.Log

/**
 * Manejador centralizado de errores de la API con mensajes específicos para el usuario
 */
object ApiErrorHandler {
    private const val TAG = "ApiErrorHandler"

    /**
     * Convertir códigos de error HTTP a mensajes amigables para el usuario
     */
    fun getErrorMessage(code: Int, endpoint: String = ""): String {
        return when (code) {
            400 -> "Error en la solicitud. Verifica los datos ingresados."
            401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
            403 -> "No tienes permisos para acceder a esta información."
            404 -> getNotFoundMessage(endpoint)
            408 -> "Tiempo de espera agotado. Verifica tu conexión a internet."
            429 -> "Demasiadas solicitudes. Intenta nuevamente en unos minutos."
            500 -> "Error interno del servidor. El equipo técnico ha sido notificado."
            502 -> "Servidor no disponible temporalmente. Intenta más tarde."
            503 -> "Servicio no disponible. Intenta más tarde."
            504 -> "Tiempo de respuesta agotado. Verifica tu conexión."
            else -> "Error de conexión (código $code). Verifica tu internet."
        }
    }

    /**
     * Mensajes específicos para errores 404 según el endpoint
     */
    private fun getNotFoundMessage(endpoint: String): String {
        Log.w(TAG, "404 Error on endpoint: $endpoint")

        return when {
            endpoint.contains("public/data") ->
                "Los datos públicos no están disponibles aún. Inicia sesión para ver más información."
            endpoint.contains("public-charts") ->
                "Los gráficos públicos no están disponibles. Inicia sesión para acceder a gráficos detallados."
            endpoint.contains("stations-measurement") ->
                "Los datos meteorológicos detallados requieren autenticación."
            endpoint.contains("stations") ->
                "La estación solicitada no fue encontrada."
            endpoint.contains("sensors") ->
                "Los sensores solicitados no están disponibles."
            else ->
                "El recurso solicitado no está disponible en este momento."
        }
    }

    /**
     * Manejar errores de conectividad y timeout
     */
    fun getNetworkErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Tiempo de conexión agotado. Verifica tu conexión a internet."
            exception.message?.contains("connection", ignoreCase = true) == true ->
                "No se pudo conectar al servidor. Verifica tu conexión a internet."
            exception.message?.contains("ssl", ignoreCase = true) == true ->
                "Error de seguridad en la conexión. Verifica la fecha y hora de tu dispositivo."
            exception.message?.contains("unknown host", ignoreCase = true) == true ->
                "No se pudo conectar al servidor. Verifica tu conexión a internet."
            else ->
                "Error de conexión. Verifica tu internet e intenta nuevamente."
        }
    }

    /**
     * Determinar si un error es recuperable (se puede reintentar)
     */
    fun isRetryableError(code: Int): Boolean {
        return when (code) {
            408, 429, 500, 502, 503, 504 -> true
            else -> false
        }
    }

    /**
     * Determinar si un error requiere reautenticación
     */
    fun requiresReauth(code: Int): Boolean {
        return code == 401
    }

    /**
     * Obtener mensaje específico para funcionalidades no disponibles
     */
    fun getFeatureUnavailableMessage(feature: String): String {
        return when (feature.lowercase()) {
            "historical_data" ->
                "📊 Los datos históricos están disponibles solo para usuarios registrados.\n\nInicia sesión para acceder a gráficos detallados y análisis temporal."
            "charts" ->
                "📈 Los gráficos avanzados requieren autenticación.\n\nInicia sesión para ver tendencias y análisis detallados."
            "public_data" ->
                "🔒 Esta funcionalidad no está disponible públicamente aún.\n\nEl equipo técnico está trabajando para habilitarla."
            "admin_features" ->
                "👮‍♂️ Esta función requiere permisos de administrador.\n\nContacta al administrador del sistema si necesitas acceso."
            else ->
                "⚠️ Esta funcionalidad no está disponible actualmente.\n\nIntenta más tarde o contacta al soporte técnico."
        }
    }
}