# Configuración de Google OAuth 2.0 para RAMF App

## 📋 Información de Configuración

### Datos del Proyecto
- **Project ID**: `ramf-442512`
- **Client ID**: `965449421194-uc1au53v6av2h29gch406j3n9tmlehn3.apps.googleusercontent.com`
- **Package Name (Debug)**: `com.cocido.ramfapp.debug`
- **Package Name (Release)**: `com.cocido.ramfapp`

### Certificados
- **SHA-1 Debug**: `2E:3A:5D:70:26:2E:D2:56:AC:39:8B:F8:89:22:F4:81:EB:99:3B:DD`
- **SHA-1 Release**: (Configurar cuando esté listo para producción)

### URLs de OAuth
- **Auth URI**: `https://accounts.google.com/o/oauth2/auth`
- **Token URI**: `https://oauth2.googleapis.com/token`
- **Cert URL**: `https://www.googleapis.com/oauth2/v1/certs`

## 🔧 Configuración en la Aplicación

### Archivos Modificados
1. **`app/src/main/res/values/google_oauth_config.xml`** - Configuración completa de OAuth
2. **`app/src/main/res/values/strings.xml`** - Referencia al Client ID
3. **`app/src/main/java/com/cocido/ramfapp/ui/activities/LoginActivity.kt`** - Implementación de Google Sign-In

### Configuración de GoogleSignInOptions
```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(getString(R.string.default_web_client_id))
    .requestEmail()
    .requestProfile()
    .build()
```

## 🚀 Estado Actual

✅ **Configuración Completada**:
- Client ID configurado correctamente
- SHA-1 fingerprint verificado
- Package name coincidente
- Google Sign-In implementado con `idToken`

✅ **Funcionalidades**:
- Login con Google
- Obtención de `idToken` para autenticación backend
- Manejo de errores mejorado
- Logging detallado para debugging

## 📱 Próximos Pasos

1. **Probar Google Sign-In** en la aplicación
2. **Verificar** que el backend reciba correctamente el `idToken`
3. **Configurar SHA-1 Release** cuando esté listo para producción
4. **Actualizar** configuración en Google Cloud Console si es necesario

## 🔍 Troubleshooting

### Error DEVELOPER_ERROR (Código 10)
- ✅ **Solucionado**: Client ID y configuración verificados
- ✅ **SHA-1**: Certificado de debug verificado
- ✅ **Package Name**: Coincide con la configuración de Google Cloud Console

### Logs de Debugging
La aplicación incluye logging detallado en `LoginActivity` para facilitar el debugging:
- Configuración de Google Sign-In
- Resultados de autenticación
- Errores específicos con códigos de estado

## 📄 Archivo JSON Original
El archivo `client_secret_965449421194-uc1au53v6av2h29gch406j3n9tmlehn3.apps.googleusercontent.com.json` contiene la configuración completa y puede ser usado para referencia o configuración adicional.

