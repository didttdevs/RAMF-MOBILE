# 🔧 Solución para Error DEVELOPER_ERROR en Google Sign-In

## 🚨 Problema Actual
**Error**: `DEVELOPER_ERROR` (código 10) al intentar usar Google Sign-In con `requestIdToken()`

## 🔍 Causa del Problema
El error ocurre porque estamos intentando usar `requestIdToken()` con un **Android Client ID**, pero esta función requiere un **Web Client ID**.

## ✅ Solución Temporal Implementada
Hemos configurado Google Sign-In sin `requestIdToken()` para que funcione con el Android Client ID actual:

```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .requestProfile()
    .build()
```

## 🎯 Solución Definitiva: Crear Web Client ID

### Paso 1: Ir a Google Cloud Console
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Selecciona tu proyecto `ramf-442512`

### Paso 2: Habilitar Google+ API (si no está habilitada)
1. Ve a **APIs & Services** > **Library**
2. Busca "Google+ API" o "Google Identity"
3. Habilítala si no está habilitada

### Paso 3: Crear Web Client ID
1. Ve a **APIs & Services** > **Credentials**
2. Haz clic en **+ CREATE CREDENTIALS** > **OAuth 2.0 Client IDs**
3. Selecciona **Web application**
4. Configura:
   - **Name**: `RAMF Web Client`
   - **Authorized JavaScript origins**: `https://your-domain.com` (opcional)
   - **Authorized redirect URIs**: `https://your-domain.com/auth/google/callback` (opcional)

### Paso 4: Obtener Web Client ID
1. Copia el **Client ID** del Web Client (será diferente al Android Client ID)
2. Actualiza el archivo `google_oauth_config.xml`:

```xml
<string name="google_oauth_web_client_id">TU_WEB_CLIENT_ID_AQUI</string>
```

### Paso 5: Actualizar LoginActivity
```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(getString(R.string.google_oauth_web_client_id))
    .requestEmail()
    .requestProfile()
    .build()
```

## 📋 Configuración Actual vs. Recomendada

### ✅ Configuración Actual (Funcional)
- **Client ID**: Android Client ID
- **Funcionalidad**: Login básico con email y perfil
- **Backend**: Recibe `google_id` en lugar de `id_token`

### 🎯 Configuración Recomendada (Con Web Client ID)
- **Client ID**: Web Client ID para `requestIdToken()`
- **Funcionalidad**: Login completo con `id_token`
- **Backend**: Recibe `id_token` para verificación completa

## 🔄 Migración Gradual

### Fase 1: Configuración Actual (Ya implementada)
```kotlin
// Backend recibe estos datos:
{
    "email": "usuario@gmail.com",
    "name": "Nombre",
    "lastName": "Apellido", 
    "avatar": "https://...",
    "google_id": "1234567890"
}
```

### Fase 2: Con Web Client ID (Futuro)
```kotlin
// Backend recibe estos datos:
{
    "email": "usuario@gmail.com",
    "name": "Nombre",
    "lastName": "Apellido",
    "avatar": "https://...",
    "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 🛠️ Verificación de Configuración

### Comprobar SHA-1 Fingerprint
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### Comprobar Package Name
- **Debug**: `com.cocido.ramfapp.debug`
- **Release**: `com.cocido.ramfapp`

## 📱 Estado Actual
- ✅ **Google Sign-In**: Funciona sin `id_token`
- ✅ **Autenticación**: Básica con datos de perfil
- ✅ **Backend**: Preparado para recibir datos básicos
- ⚠️ **Limitación**: Sin verificación de `id_token` en backend

## 🚀 Próximos Pasos
1. **Probar** la configuración actual
2. **Crear Web Client ID** cuando sea necesario
3. **Migrar** a configuración completa con `id_token`
4. **Verificar** funcionamiento en backend

¡La aplicación debería funcionar correctamente ahora! 🎉

