# 🔑 Configuración de Web Client ID para Google Sign-In

## 🚨 **Problema Identificado**

El backend requiere `idToken` para autenticación con Google:
```json
{
    "message": "Falta idToken",
    "error": "Unauthorized",
    "statusCode": 401
}
```

## ✅ **Solución: Crear Web Client ID**

### **Paso 1: Ir a Google Cloud Console**
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Selecciona tu proyecto: **`ramf-442512`**

### **Paso 2: Crear Web Client ID**
1. Ve a **APIs & Services** → **Credentials**
2. Haz clic en **+ CREATE CREDENTIALS** → **OAuth 2.0 Client IDs**
3. Selecciona **Web application**
4. Configura:
   - **Name**: `RAMF Web Client`
   - **Authorized JavaScript origins**: (dejar vacío)
   - **Authorized redirect URIs**: (dejar vacío)
5. Haz clic en **CREATE**

### **Paso 3: Copiar Web Client ID**
1. Se mostrará una ventana con el nuevo Client ID
2. **Copia el Client ID** (será diferente al Android Client ID)
3. Ejemplo: `965449421194-abc123def456.apps.googleusercontent.com`

### **Paso 4: Actualizar Configuración**
1. Abre el archivo: `app/src/main/res/values/google_oauth_config.xml`
2. Reemplaza el valor de `google_oauth_web_client_id`:

```xml
<string name="google_oauth_web_client_id">TU_WEB_CLIENT_ID_AQUI</string>
```

### **Paso 5: Verificar Configuración**
El archivo `strings.xml` debe apuntar al Web Client ID:
```xml
<string name="default_web_client_id">@string/google_oauth_web_client_id</string>
```

## 🔍 **Verificación**

### **Android Client ID vs Web Client ID**
- **Android Client ID**: `965449421194-uc1au53v6av2h29gch406j3n9tmlehn3.apps.googleusercontent.com`
- **Web Client ID**: `965449421194-XXXXXXXXXX.apps.googleusercontent.com` (diferente)

### **Uso Correcto**
- **Android Client ID**: Para autenticación básica
- **Web Client ID**: Para `requestIdToken()` (requerido por el backend)

## 📱 **Estado Actual de la App**

### **Configuración Implementada**
```kotlin
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(getString(R.string.default_web_client_id))
    .requestEmail()
    .requestProfile()
    .build()
```

### **Datos Enviados al Backend**
```json
{
    "email": "usuario@gmail.com",
    "name": "Nombre",
    "lastName": "Apellido",
    "avatar": "https://...",
    "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 🚀 **Próximos Pasos**

1. **Crear Web Client ID** en Google Cloud Console
2. **Actualizar** `google_oauth_web_client_id` en el proyecto
3. **Compilar** y probar la aplicación
4. **Verificar** que Google Sign-In funcione sin errores

## ⚠️ **Importante**

- **NO uses** el Android Client ID con `requestIdToken()`
- **SÍ usa** el Web Client ID con `requestIdToken()`
- **Ambos** deben estar en el mismo proyecto de Google Cloud

¡Una vez configurado el Web Client ID, Google Sign-In debería funcionar perfectamente! 🎉
