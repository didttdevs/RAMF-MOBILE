package com.cocido.ramfapp.common

/**
 * Professional UI State wrapper class for handling different states
 * in the UI layer with proper error handling and loading states
 */
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasData: Boolean get() = data != null && !isLoading
    val hasError: Boolean get() = error != null && !isLoading
    val isIdle: Boolean get() = !isLoading && error == null && data == null
}