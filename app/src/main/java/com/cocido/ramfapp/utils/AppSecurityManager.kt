package com.cocido.ramfapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Gestor de seguridad de la aplicación con mejores prácticas
 */
object AppSecurityManager {
    private const val TAG = "AppSecurityManager"
    
    /**
     * Verificar la integridad de la aplicación
     */
    fun verifyAppIntegrity(context: Context): SecurityStatus {
        Log.d(TAG, "Verifying app integrity")
        
        val checks = mutableListOf<SecurityCheck>()
        
        // Verificar si la aplicación está firmada correctamente
        val signatureCheck = verifyAppSignature(context)
        checks.add(signatureCheck)
        
        // Verificar si hay root/jailbreak
        val rootCheck = detectRootAccess()
        checks.add(rootCheck)
        
        // Verificar si hay debugging habilitado
        val debugCheck = detectDebugging()
        checks.add(debugCheck)
        
        // Verificar si hay emulador
        val emulatorCheck = detectEmulator()
        checks.add(emulatorCheck)
        
        // Verificar configuración de seguridad
        val securityConfigCheck = verifySecurityConfiguration(context)
        checks.add(securityConfigCheck)
        
        val status = SecurityStatus(checks)
        Log.d(TAG, "App integrity check completed: ${status.overallStatus}")
        
        return status
    }
    
    /**
     * Verificar la firma de la aplicación
     */
    private fun verifyAppSignature(context: Context): SecurityCheck {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.signingCertificateHistory
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures != null && signatures.isNotEmpty()) {
                SecurityCheck(
                    name = "App Signature",
                    status = SecurityStatus.Status.SAFE,
                    details = "App is properly signed"
                )
            } else {
                SecurityCheck(
                    name = "App Signature",
                    status = SecurityStatus.Status.WARNING,
                    details = "App signature verification failed"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying app signature", e)
            SecurityCheck(
                name = "App Signature",
                status = SecurityStatus.Status.ERROR,
                details = "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Detectar acceso root/jailbreak
     */
    private fun detectRootAccess(): SecurityCheck {
        val rootIndicators = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        val foundRoot = rootIndicators.any { path ->
            try {
                java.io.File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
        
        return if (foundRoot) {
            SecurityCheck(
                name = "Root Detection",
                status = SecurityStatus.Status.WARNING,
                details = "Root access detected"
            )
        } else {
            SecurityCheck(
                name = "Root Detection",
                status = SecurityStatus.Status.SAFE,
                details = "No root access detected"
            )
        }
    }
    
    /**
     * Detectar debugging habilitado
     */
    private fun detectDebugging(): SecurityCheck {
        val isDebuggable = try {
            val buildConfig = Class.forName("com.cocido.ramfapp.BuildConfig")
            buildConfig.getField("DEBUG").getBoolean(null)
        } catch (e: Exception) {
            false
        }
        
        return if (isDebuggable) {
            SecurityCheck(
                name = "Debug Mode",
                status = SecurityStatus.Status.WARNING,
                details = "Debug mode is enabled"
            )
        } else {
            SecurityCheck(
                name = "Debug Mode",
                status = SecurityStatus.Status.SAFE,
                details = "Debug mode is disabled"
            )
        }
    }
    
    /**
     * Detectar emulador
     */
    private fun detectEmulator(): SecurityCheck {
        val emulatorIndicators = listOf(
            Build.FINGERPRINT.startsWith("generic"),
            Build.FINGERPRINT.contains("unknown"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK built for x86"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("ranchu"),
            Build.PRODUCT.contains("sdk"),
            Build.PRODUCT.contains("google_sdk"),
            Build.PRODUCT.contains("sdk_gphone"),
            Build.PRODUCT.contains("sdk_gphone_x86"),
            Build.BOARD.contains("goldfish"),
            Build.BOARD.contains("ranchu")
        )
        
        val isEmulator = emulatorIndicators.any { it }
        
        return if (isEmulator) {
            SecurityCheck(
                name = "Emulator Detection",
                status = SecurityStatus.Status.WARNING,
                details = "Running on emulator"
            )
        } else {
            SecurityCheck(
                name = "Emulator Detection",
                status = SecurityStatus.Status.SAFE,
                details = "Running on real device"
            )
        }
    }
    
    /**
     * Verificar configuración de seguridad
     */
    private fun verifySecurityConfiguration(context: Context): SecurityCheck {
        val issues = mutableListOf<String>()
        
        // Verificar si EncryptedSharedPreferences está disponible
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            
            EncryptedSharedPreferences.create(
                "test_security",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            issues.add("EncryptedSharedPreferences not available")
        }
        
        return if (issues.isEmpty()) {
            SecurityCheck(
                name = "Security Configuration",
                status = SecurityStatus.Status.SAFE,
                details = "Security features properly configured"
            )
        } else {
            SecurityCheck(
                name = "Security Configuration",
                status = SecurityStatus.Status.WARNING,
                details = "Issues found: ${issues.joinToString(", ")}"
            )
        }
    }
    
    /**
     * Generar hash seguro de datos
     */
    fun generateSecureHash(data: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(data.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "SHA-256 algorithm not available", e)
            data.hashCode().toString()
        }
    }
    
    /**
     * Verificar si el dispositivo es seguro para usar
     */
    fun isDeviceSecure(context: Context): Boolean {
        val securityStatus = verifyAppIntegrity(context)
        return securityStatus.overallStatus == SecurityStatus.Status.SAFE
    }
}

/**
 * Estado de seguridad de la aplicación
 */
data class SecurityStatus(
    val checks: List<SecurityCheck>,
    val overallStatus: Status = determineOverallStatus(checks)
) {
    enum class Status {
        SAFE,
        WARNING,
        ERROR
    }
    
    companion object {
        private fun determineOverallStatus(checks: List<SecurityCheck>): Status {
            return when {
                checks.any { it.status == Status.ERROR } -> Status.ERROR
                checks.any { it.status == Status.WARNING } -> Status.WARNING
                else -> Status.SAFE
            }
        }
    }
}

/**
 * Verificación de seguridad individual
 */
data class SecurityCheck(
    val name: String,
    val status: SecurityStatus.Status,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
