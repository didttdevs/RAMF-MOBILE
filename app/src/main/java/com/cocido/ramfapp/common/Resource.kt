package com.cocido.ramfapp.common

/**
 * Generic resource wrapper for handling network responses and loading states
 * Following Clean Architecture principles with proper error handling
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Throwable, String?) -> Unit): Resource<T> {
        if (this is Error) action(exception, message)
        return this
    }

    inline fun onLoading(action: () -> Unit): Resource<T> {
        if (this is Loading) action()
        return this
    }
}


/**
 * Network result wrapper with detailed error information
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val code: Int? = null,
        val message: String,
        val cause: Throwable? = null
    ) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}