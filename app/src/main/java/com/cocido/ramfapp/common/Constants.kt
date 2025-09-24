package com.cocido.ramfapp.common

/**
 * Application constants following security and maintainability best practices
 */
object Constants {

    // Network Configuration
    object Network {
        const val CONNECTION_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1000L
        const val CACHE_MAX_AGE = 5 * 60 // 5 minutes
        const val CACHE_MAX_STALE = 24 * 60 * 60 // 24 hours offline
    }

    // Authentication
    object Auth {
        const val TOKEN_REFRESH_BUFFER_MINUTES = 5
        const val SESSION_TIMEOUT_MINUTES = 20
        const val MAX_LOGIN_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MINUTES = 15
    }

    // Data Validation
    object Validation {
        const val MIN_TEMPERATURE = -50.0
        const val MAX_TEMPERATURE = 60.0
        const val MIN_HUMIDITY = 0.0
        const val MAX_HUMIDITY = 100.0
        const val MAX_WIND_SPEED = 200.0
        const val MIN_PRESSURE = 800.0
        const val MAX_PRESSURE = 1200.0
    }

    // UI Constants
    object UI {
        const val ANIMATION_DURATION = 300L
        const val DEBOUNCE_DELAY = 500L
        const val REFRESH_INTERVAL = 60000L // 1 minute
    }

    // Logging
    object Logging {
        const val MAX_LOG_LENGTH = 4000
        const val SENSITIVE_DATA_MASK = "***"
        val SENSITIVE_FIELDS = setOf(
            "password", "token", "accessToken", "refreshToken",
            "authorization", "secret", "key", "credential"
        )
    }

    // Security
    object Security {
        const val MIN_PASSWORD_LENGTH = 8
        const val ENCRYPTION_KEY_ALIAS = "ramf_app_key"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}