# Configuración de Google Maps API

## Problema Identificado
La API key de Google Maps está configurada en `AndroidManifest.xml` pero hay un problema de autorización:

```
Authorization failure. Please see https://developers.google.com/maps/documentation/android-sdk/start
```

## Solución Requerida

### 1. En Google Cloud Console
1. Ir a [Google Cloud Console](https://console.cloud.google.com)
2. Seleccionar el proyecto correcto
3. Ir a **APIs & Services > Credentials**
4. Editar la API key: `AIzaSyBTC17-39zuMCoUcY_kq9VyTP8HEEZFWkw`

### 2. Configurar Restricciones de Aplicación Android

#### Para Debug Build:
- **Application restrictions**: Android apps
- **Package name**: `com.cocido.ramfapp.debug`
- **SHA-1 certificate fingerprint**: `2E:3A:5D:70:26:2E:D2:56:AC:39:8B:F8:89:22:F4:81:EB:99:3B:DD`

#### Para Release Build:
- **Package name**: `com.cocido.ramfapp`
- **SHA-1 certificate fingerprint**: [Necesita obtenerse del keystore de release]

### 3. APIs Habilitadas Requeridas
Asegurarse de que estén habilitadas:
- Maps SDK for Android
- Places API (si se usa)
- Geocoding API (si se usa)

### 4. Verificar Package Name
En `app/build.gradle`:
```gradle
android {
    namespace 'com.cocido.ramfapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.cocido.ramfapp"
        // ...
    }

    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            // ...
        }
    }
}
```

### 5. Cómo Obtener SHA-1 del Keystore

#### Para Debug:
```bash
keytool -list -v -keystore "C:\Users\Matias\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### Para Release:
```bash
keytool -list -v -keystore path/to/release.keystore -alias your_key_alias
```

## Estado Actual
- ✅ API Key configurada en AndroidManifest.xml
- ✅ SHA-1 fingerprint del debug keystore obtenido
- ❌ Restricciones de aplicación no configuradas correctamente en Google Cloud Console
- ❌ Possibly missing required APIs enabled

## Próximos Pasos
1. Acceder a Google Cloud Console
2. Configurar las restricciones según se especifica arriba
3. Probar la aplicación nuevamente
4. Si persiste el error, verificar que las APIs requeridas estén habilitadas