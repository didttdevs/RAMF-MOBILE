package com.cocido.ramfapp.utils

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Professional Security Logger implementing DevSecOps best practices
 * with structured logging, audit trails, and security monitoring
 */
class SecurityLogger {
    private val TAG = "SecurityLogger"
    private val SECURITY_LOG_TAG = "SECURITY_AUDIT"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    /**
     * Security levels with proper severity classification
     */
    enum class SecurityLevel(val severity: Int) {
        INFO(1),        // Normal operational events
        WARNING(2),     // Events requiring attention
        ERROR(3),       // Error events that don't compromise security
        CRITICAL(4),    // Critical security events requiring immediate attention
        FATAL(5)        // Fatal security violations
    }

    /**
     * Comprehensive event types following security audit standards
     */
    enum class EventType {
        // Authentication Events
        AUTH_LOGIN_SUCCESS,
        AUTH_LOGIN_FAILED,
        AUTH_LOGOUT,
        AUTH_TOKEN_REFRESH,
        AUTH_TOKEN_EXPIRED,
        AUTH_SESSION_TIMEOUT,
        AUTH_BRUTE_FORCE_DETECTED,

        // Authorization Events
        PERMISSION_GRANTED,
        PERMISSION_DENIED,
        ACCESS_CONTROL_VIOLATION,

        // API Security Events
        API_ACCESS_SUCCESS,
        API_ACCESS_FAILED,
        API_RATE_LIMIT_EXCEEDED,
        API_MALFORMED_REQUEST,

        // Data Security Events
        DATA_ACCESS,
        DATA_MODIFICATION,
        DATA_EXPORT,
        SENSITIVE_DATA_ACCESS,

        // Network Security Events
        NETWORK_REQUEST,
        NETWORK_ERROR,
        NETWORK_TIMEOUT,
        SSL_CERTIFICATE_ERROR,

        // Application Security Events
        APP_START,
        APP_CRASH,
        APP_BACKGROUND,
        APP_FOREGROUND,

        // Security Violations
        SECURITY_VIOLATION,
        SUSPICIOUS_ACTIVITY,
        MALICIOUS_INPUT_DETECTED,

        // User Actions
        USER_ACTION,
        USER_NAVIGATION,
        USER_ERROR
    }

    /**
     * Professional security event logging with structured data
     */
    private fun logSecurityEvent(
        eventType: EventType,
        level: SecurityLevel,
        message: String,
        userId: String? = null,
        additionalData: Map<String, Any>? = null
    ) {
        val timestamp = dateFormat.format(Date())
        val currentUserId = userId ?: AuthManager.getCurrentUser()?.id

        val logMessage = buildString {
            append("[$timestamp] ")
            append("[${level.name}] ")
            append("[${eventType.name}] ")
            append("[User: ${currentUserId ?: "anonymous"}] ")
            append(message)

            additionalData?.let { data ->
                append(" | Data: ")
                append(data.entries.joinToString(", ") { "${it.key}=${it.value}" })
            }
        }

        // Log according to security level
        when (level) {
            SecurityLevel.INFO -> Log.i(SECURITY_LOG_TAG, logMessage)
            SecurityLevel.WARNING -> Log.w(SECURITY_LOG_TAG, logMessage)
            SecurityLevel.ERROR -> Log.e(SECURITY_LOG_TAG, logMessage)
            SecurityLevel.CRITICAL -> Log.e(SECURITY_LOG_TAG, logMessage)
            SecurityLevel.FATAL -> Log.wtf(SECURITY_LOG_TAG, logMessage)
        }

        // In production, here you could send logs to monitoring service
        // like Firebase Crashlytics, Sentry, or your own logging system
    }

    /**
     * Enhanced data access logging with validation
     */
    fun logDataAccess(
        dataType: String,
        recordCount: Int,
        stationName: String? = null,
        operation: String = "read"
    ) {
        logSecurityEvent(
            eventType = EventType.DATA_ACCESS,
            level = SecurityLevel.INFO,
            message = "Data access: $operation on $dataType",
            additionalData = mapOf(
                "data_type" to dataType,
                "record_count" to recordCount,
                "station_name" to (stationName ?: "N/A"),
                "operation" to operation
            )
        )
    }

    // MARK: - Public Security Event Methods

    /**
     * Log authentication events with enhanced security context
     */
    fun logAuthenticationEvent(
        eventType: String,
        success: Boolean,
        method: String = "email",
        additionalInfo: String? = null
    ) {
        val level = if (success) SecurityLevel.INFO else SecurityLevel.WARNING
        val event = if (success) EventType.AUTH_LOGIN_SUCCESS else EventType.AUTH_LOGIN_FAILED

        logSecurityEvent(
            eventType = event,
            level = level,
            message = "Authentication $eventType: ${if (success) "successful" else "failed"}",
            additionalData = mapOf(
                "method" to method,
                "success" to success,
                "additional_info" to (additionalInfo ?: "none")
            )
        )
    }

    /**
     * Log network security events
     */
    fun logNetworkSecurityEvent(
        endpoint: String,
        statusCode: Int,
        method: String = "GET",
        responseTime: Long? = null
    ) {
        val level = when {
            statusCode in 200..299 -> SecurityLevel.INFO
            statusCode == 401 -> SecurityLevel.WARNING
            statusCode == 403 -> SecurityLevel.WARNING
            statusCode >= 500 -> SecurityLevel.WARNING
            else -> SecurityLevel.WARNING
        }

        logSecurityEvent(
            eventType = EventType.NETWORK_REQUEST,
            level = level,
            message = "Network request: $method $endpoint",
            additionalData = mapOf(
                "endpoint" to endpoint,
                "method" to method,
                "status_code" to statusCode,
                "response_time" to (responseTime ?: 0)
            )
        )
    }

    /**
     * Log application lifecycle security events
     */
    fun logAppSecurityEvent(
        eventType: String,
        context: Context? = null
    ) {
        val appVersion = context?.let {
            try {
                it.packageManager.getPackageInfo(it.packageName, 0).versionName
            } catch (e: Exception) {
                "unknown"
            }
        } ?: "unknown"

        logSecurityEvent(
            eventType = EventType.APP_START,
            level = SecurityLevel.INFO,
            message = "Application $eventType",
            additionalData = mapOf(
                "app_version" to appVersion,
                "event_type" to eventType
            )
        )
    }

    /**
     * Log user interaction security events
     */
    fun logUserSecurityEvent(
        action: String,
        screen: String,
        success: Boolean = true,
        additionalInfo: String? = null
    ) {
        logSecurityEvent(
            eventType = EventType.USER_ACTION,
            level = SecurityLevel.INFO,
            message = "User action: $action on $screen",
            additionalData = mapOf(
                "action" to action,
                "screen" to screen,
                "success" to success,
                "additional_info" to (additionalInfo ?: "none")
            )
        )
    }

    /**
     * Log critical security violations
     */
    fun logSecurityViolation(
        violationType: String,
        severity: SecurityLevel = SecurityLevel.CRITICAL,
        details: String? = null
    ) {
        logSecurityEvent(
            eventType = EventType.SECURITY_VIOLATION,
            level = severity,
            message = "Security violation: $violationType",
            additionalData = mapOf(
                "violation_type" to violationType,
                "details" to (details ?: "none")
            )
        )
    }
}



