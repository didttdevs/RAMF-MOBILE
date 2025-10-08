# 🔐 Configuración de Keystore para Release - RAF App

## Generar Keystore de Producción

### Paso 1: Generar el keystore

```bash
keytool -genkey -v -keystore release-keystore.jks \
  -alias ramf-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD
```

**Importante:**
- Guarda el keystore en un lugar seguro (NO lo subas a Git)
- Anota las contraseñas en un gestor de contraseñas seguro
- El keystore es IRREEMPLAZABLE - si lo pierdes, no podrás actualizar la app en Play Store

### Paso 2: Configurar variables de entorno

#### Windows (PowerShell)
```powershell
$env:RAMF_KEYSTORE_PATH="C:\path\to\release-keystore.jks"
$env:RAMF_KEYSTORE_PASSWORD="YOUR_STORE_PASSWORD"
$env:RAMF_KEY_ALIAS="ramf-app"
$env:RAMF_KEY_PASSWORD="YOUR_KEY_PASSWORD"
```

#### Linux/Mac (Terminal)
```bash
export RAMF_KEYSTORE_PATH="/path/to/release-keystore.jks"
export RAMF_KEYSTORE_PASSWORD="YOUR_STORE_PASSWORD"
export RAMF_KEY_ALIAS="ramf-app"
export RAMF_KEY_PASSWORD="YOUR_KEY_PASSWORD"
```

### Paso 3: Opción alternativa - gradle.properties local

Crear archivo `gradle.properties` en la raíz del proyecto (este archivo DEBE estar en .gitignore):

```properties
RAMF_KEYSTORE_PATH=/path/to/release-keystore.jks
RAMF_KEYSTORE_PASSWORD=YOUR_STORE_PASSWORD
RAMF_KEY_ALIAS=ramf-app
RAMF_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

Luego actualizar `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(project.findProperty("RAMF_KEYSTORE_PATH") as String? ?: "release-keystore.jks")
        storePassword = project.findProperty("RAMF_KEYSTORE_PASSWORD") as String? ?: ""
        keyAlias = project.findProperty("RAMF_KEY_ALIAS") as String? ?: "ramf-app"
        keyPassword = project.findProperty("RAMF_KEY_PASSWORD") as String? ?: ""
    }
}
```

## Generar APK/AAB de Release

### APK (para distribución directa)
```bash
./gradlew assembleRelease
```

El APK estará en: `app/build/outputs/apk/release/app-release.apk`

### AAB (para Play Store - RECOMENDADO)
```bash
./gradlew bundleRelease
```

El AAB estará en: `app/build/outputs/bundle/release/app-release.aab`

## Verificar firma del APK

```bash
jarsigner -verify -verbose -certs app-release.apk
```

## Generar SHA-1 y SHA-256 para Google Services

```bash
keytool -list -v -keystore release-keystore.jks -alias ramf-app
```

**Importante:** Agrega estos fingerprints en Firebase Console:
1. Ve a Project Settings
2. Agrega el SHA-1 y SHA-256 en tu app Android
3. Descarga el nuevo `google-services.json`

## Build Types Disponibles

### Debug
```bash
./gradlew assembleDebug
```
- Package: `com.cocido.ramfapp.debug`
- Debuggable, sin ofuscación
- Usa firma de debug

### Staging
```bash
./gradlew assembleStaging
```
- Package: `com.cocido.ramfapp.staging`
- Debuggable pero con ofuscación R8
- Para pruebas pre-producción

### Release
```bash
./gradlew assembleRelease
```
- Package: `com.cocido.ramfapp`
- Ofuscado, optimizado
- Usa firma de release

## Seguridad

### ✅ HACER:
- Guardar keystore en múltiples ubicaciones seguras
- Usar gestor de contraseñas para las claves
- Agregar `*.jks` y `gradle.properties` a `.gitignore`
- Hacer backup del keystore en almacenamiento encriptado

### ❌ NO HACER:
- Subir keystore a Git/GitHub
- Compartir contraseñas en texto plano
- Usar contraseñas débiles
- Perder el keystore (es IRREEMPLAZABLE)

## CI/CD (GitHub Actions, etc.)

Para CI/CD, usa secretos encriptados:

```yaml
- name: Build Release
  env:
    RAMF_KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    RAMF_KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew bundleRelease
```

