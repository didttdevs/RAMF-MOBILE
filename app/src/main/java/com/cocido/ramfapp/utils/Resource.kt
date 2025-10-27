package com.cocido.ramfapp.utils

/**
 * Clase genérica para manejar estados de recursos
 * Basado en el patrón Resource de Android
 */
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}


