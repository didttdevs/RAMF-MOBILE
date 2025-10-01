# Documentación de Cambios - 01 Octubre 2025

## Resumen General
Se realizaron correcciones críticas en la aplicación RAF para solucionar problemas de visualización de datos meteorológicos y configuración del mapa.

---

## 1. CORRECCIÓN DE DATOS METEOROLÓGICOS

### Problema Identificado
La aplicación mostraba datos meteorológicos incorrectos (ej: humedad relativa 99.98% en lugar de 85.28%) porque estaba presentando datos históricos del día anterior en lugar de los datos actuales del widget.

### Root Cause Analysis
1. **Mapeo JSON Incorrecto**: Los nombres de campos en `WidgetData.kt` no coincidían con la respuesta de la API
   - API devuelve: `relativeHumidity` (camelCase)
   - Modelo esperaba: `relative_humidity` (snake_case)

2. **Conflicto en WeatherInfoFragment**: Dos observadores corrían en paralelo:
   - Observador de `widgetData` (datos correctos)
   - Observador de `historicalData` (datos del día anterior que sobrescribían los correctos)

### Soluciones Implementadas

#### 1.1 Corrección del Mapeo JSON en `WidgetData.kt`
```kotlin
// ANTES (snake_case)
@SerializedName("max_temperature") val maxTemperature: Double
@SerializedName("min_temperature") val minTemperature: Double
@SerializedName("relative_humidity") val relativeHumidity: Double
@SerializedName("dew_point") val dewPoint: Double
@SerializedName("air_pressure") val airPressure: Double
@SerializedName("solar_radiation") val solarRadiation: Double
@SerializedName("wind_speed") val windSpeed: Double
@SerializedName("wind_direction") val windDirection: String
@SerializedName("rain_last_hour") val rainLastHour: Double
@SerializedName("rain_day") val rainDay: Double

// DESPUÉS (camelCase)
@SerializedName("maxTemperature") val maxTemperature: Double
@SerializedName("minTemperature") val minTemperature: Double
@SerializedName("relativeHumidity") val relativeHumidity: Double
@SerializedName("dewPoint") val dewPoint: Double
@SerializedName("airPressure") val airPressure: Double
@SerializedName("solarRadiation") val solarRadiation: Double
@SerializedName("windSpeed") val windSpeed: Double
@SerializedName("windDirection") val windDirection: String
@SerializedName("rainLastHour") val rainLastHour: Double
@SerializedName("rainDay") val rainDay: Double
```

#### 1.2 Corrección de Validación en `WeatherRepository.kt`
```kotlin
// ANTES - Rechazaba humedad 0.0% como inválida
val humidityValid = data.relativeHumidity >= Constants.Validation.MIN_HUMIDITY &&
        data.relativeHumidity <= Constants.Validation.MAX_HUMIDITY &&
        data.relativeHumidity != 0.0

// DESPUÉS - Permitir humedad 0.0% ya que puede ser válida
val humidityValid = data.relativeHumidity >= Constants.Validation.MIN_HUMIDITY &&
        data.relativeHumidity <= Constants.Validation.MAX_HUMIDITY
```

#### 1.3 Corrección de Lógica de Presentación en `WeatherInfoFragment.kt`
```kotlin
// ANTES - Dos observadores en paralelo causaban conflicto
lifecycleScope.launch {
    viewModel.widgetData.collect { widgetState ->
        if (widgetState.hasData) {
            updateWeatherDataFromWidget(widgetState.data!!)
        }
    }
}

lifecycleScope.launch {
    viewModel.historicalData.collect { historicalState ->
        if (historicalState.hasData) {
            val latest = historicalState.data!!.firstOrNull()
            updateWeatherDataFromSensors(latest)
        }
    }
}

// DESPUÉS - Un solo observador con fallback lógico
lifecycleScope.launch {
    viewModel.widgetData.collect { widgetState ->
        if (widgetState.hasData) {
            updateWeatherDataFromWidget(widgetState.data!!)
        } else {
            // Solo usar datos históricos como fallback cuando NO hay datos del widget
            viewModel.historicalData.value.let { historicalState ->
                if (historicalState.hasData) {
                    val latest = historicalState.data!!.firstOrNull()
                    updateWeatherDataFromSensors(latest)
                }
            }
        }
    }
}
```

### Resultado
✅ La aplicación ahora muestra datos meteorológicos correctos y actuales
✅ Los datos coinciden exactamente con los mostrados en la página web

---

## 2. CORRECCIÓN DE FORMATOS DE VISUALIZACIÓN

### Problema Identificado
Los datos se mostraban con formatos incorrectos:
- Presión: 4 decimales en hPa
- Humedad: 1 decimal (debería ser 2)
- Precipitaciones: muchos decimales innecesarios
- Velocidad del viento: unidades incorrectas

### Soluciones Implementadas

#### 2.1 Nuevos Formatos en `WidgetData.kt`
```kotlin
// Humedad relativa: 2 decimales
fun getFormattedHumidity(): String {
    return if (isValidValue(relativeHumidity)) "${String.format("%.2f", relativeHumidity)}%" else "N/A"
}

// Presión atmosférica: 1 decimal en kPa
fun getFormattedPressure(): String {
    return if (isValidValue(airPressure)) "${String.format("%.1f", airPressure)} kPa" else "N/A"
}

// Velocidad del viento: 2 decimales en km/h
fun getFormattedWindSpeed(): String {
    return if (isValidNonZeroValue(windSpeed)) "${String.format("%.2f", windSpeed)} km/h" else "N/A"
}

// Radiación solar: 2 decimales
fun getFormattedSolarRadiation(): String {
    return if (isValidNonZeroValue(solarRadiation)) "${String.format("%.2f", solarRadiation)} W/m²" else "N/A"
}

// Precipitaciones: 1 decimal
fun getFormattedRainLastHour(): String {
    return "${String.format("%.1f", rainLastHour)} mm"
}

fun getFormattedRain24h(): String {
    return "${String.format("%.1f", rain24h)} mm"
}

fun getFormattedRain48h(): String {
    return "${String.format("%.1f", rain48h)} mm"
}

fun getFormattedRain7d(): String {
    return "${String.format("%.1f", rain7d)} mm"
}
```

#### 2.2 Actualización en `WeatherInfoFragment.kt`
```kotlin
// ANTES - Sin formato para precipitaciones
binding.rainLast1hTextView.text = "${widget.rainLastHour} mm"
binding.rainLast24hTextView.text = "${widget.rain24h} mm"
binding.rainLast48hTextView.text = "${widget.rain48h} mm"
binding.rainLast7dTextView.text = "${widget.rain7d} mm"

// DESPUÉS - Con funciones de formato
binding.rainLast1hTextView.text = widget.getFormattedRainLastHour()
binding.rainLast24hTextView.text = widget.getFormattedRain24h()
binding.rainLast48hTextView.text = widget.getFormattedRain48h()
binding.rainLast7dTextView.text = widget.getFormattedRain7d()
```

### Resultado
✅ Formatos profesionales y consistentes:
- Presión: `101.4 kPa` (1 decimal)
- Humedad: `85.28%` (2 decimales)
- Velocidad del viento: `4.00 km/h` (2 decimales)
- Radiación solar: `155.00 W/m²` (2 decimales)
- Precipitaciones: `15.2 mm` (1 decimal)
- Punto de rocío: `20.5°C` (1 decimal)

---

## 3. DIAGNÓSTICO Y CONFIGURACIÓN DE GOOGLE MAPS

### Problema Identificado
La funcionalidad de mapa no mostraba nada debido a problemas de configuración de Google Maps API.

### Investigación Realizada

#### 3.1 Verificación de Código
✅ **MapActivity.kt**: Implementación correcta
✅ **activity_map.xml**: Layout correcto con SupportMapFragment
✅ **Dependencias**: `google-maps` correctamente incluida
✅ **Permisos**: Configurados correctamente

#### 3.2 Verificación de Configuración
✅ **AndroidManifest.xml**: Configuración de API key presente
✅ **SHA-1 Fingerprint**: Obtenido correctamente
```bash
keytool -list -v -keystore "C:\Users\Matias\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Resultado:
SHA1: 2E:3A:5D:70:26:2E:D2:56:AC:39:8B:F8:89:22:F4:81:EB:99:3B:DD
```

#### 3.3 Corrección de API Key
**Problema encontrado**: API key en AndroidManifest.xml no coincidía con Google Cloud Console

```xml
<!-- ANTES -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyBTC17-39zuMCoUcY_kq9VyTP8HEEZFWkw" />

<!-- DESPUÉS -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyCiYvzpKOuAwnRXa3pkFqm_8OFC-wnn-Lc" />
```

### Configuración de Google Cloud Console
✅ **API Key configurada** con restricciones:
- **Application restrictions**: Android apps
- **Package name**: `com.cocido.ramfapp.debug`
- **SHA-1 certificate fingerprint**: `2E:3A:5D:70:26:2E:D2:56:AC:39:8B:F8:89:22:F4:81:EB:99:3B:DD`

### Problema Final Identificado
❌ **Maps SDK for Android requiere facturación habilitada** en Google Cloud Console

**Error en logs**:
```
E Google Android Maps SDK: Ensure that the "Maps SDK for Android" is enabled.
E Google Android Maps SDK: API Key: AIzaSyCiYvzpKOuAwnRXa3pkFqm_8OFC-wnn-Lc
E Google Android Maps SDK: Android Application: 2E:3A:5D:70:26:2E:D2:56:AC:39:8B:F8:89:22:F4:81:EB:99:3B:DD;com.cocido.ramfapp.debug
```

### Acción Requerida
🔄 **Pendiente**: Configuración de método de pago en Google Cloud Console por parte del equipo

---

## 4. ARCHIVOS MODIFICADOS

### Archivos Editados
1. **`app/src/main/java/com/cocido/ramfapp/models/WidgetData.kt`**
   - Corrección de mapeo JSON (snake_case → camelCase)
   - Nuevas funciones de formateo para precipitaciones
   - Corrección de formatos de visualización

2. **`app/src/main/java/com/cocido/ramfapp/repository/WeatherRepository.kt`**
   - Eliminación de validación incorrecta de humedad 0.0%

3. **`app/src/main/java/com/cocido/ramfapp/ui/fragments/WeatherInfoFragment.kt`**
   - Corrección de lógica de observadores
   - Implementación de funciones de formato para precipitaciones

4. **`app/src/main/AndroidManifest.xml`**
   - Actualización de API key de Google Maps

### Archivos de Documentación Generados
5. **`GOOGLE_MAPS_SETUP.md`** (ya existía)
   - Documentación completa de configuración de Google Maps

6. **`DOCUMENTACION_CAMBIOS_01_OCT_2025.md`** (este archivo)
   - Documentación completa de todos los cambios realizados

---

## 5. VERIFICACIÓN DE ENDPOINTS API

### Endpoint de Widget (Datos Actuales)
✅ **Funcionando correctamente**:
```
GET https://ramf.formosa.gob.ar/api/http/stations-measurement/widget/00210E7D

Respuesta ejemplo:
{
  "timestamp": "2025-10-01T16:00:00.000Z",
  "temperature": 23.22,
  "maxTemperature": 23.33,
  "minTemperature": 19.51,
  "relativeHumidity": 85.28,
  "dewPoint": 20.5,
  "airPressure": 101.3595,
  "solarRadiation": 155,
  "windSpeed": 4,
  "windDirection": "Este Sureste, ESE",
  "rainLastHour": 0,
  "rainDay": 1,
  "rain24h": 15.199999999999994,
  "rain48h": 149.1999999999998,
  "rain7d": 168.99999999999977
}
```

### Endpoints de Datos Históricos
✅ **Requieren autenticación** (Bearer token):
```
GET https://ramf.formosa.gob.ar/api/http/stations-measurement/data-time-range/00210E7D?from=2025-10-01T00:00:00.000Z&to=2025-10-01T23:59:59.999Z&timeRange=custom
```

---

## 6. ESTADO FINAL

### ✅ Funcionalidades Completamente Operativas
- **Visualización de datos meteorológicos**: Datos correctos y actuales
- **Formatos de presentación**: Profesionales y consistentes
- **Autenticación**: Funcionando correctamente
- **Navegación**: Sin problemas
- **APIs de datos**: Todas operativas

### 🔄 Funcionalidades Pendientes
- **Mapa**: Requiere configuración de facturación en Google Cloud Console

### 📝 Próximos Pasos
1. **Configurar método de pago** en Google Cloud Console
2. **Habilitar Maps SDK for Android** una vez configurada la facturación
3. **Probar funcionalidad de mapa** después de la configuración

---

## 7. COMANDOS ÚTILES PARA DEBUGGING

### Verificar SHA-1 Fingerprint
```bash
keytool -list -v -keystore "C:\Users\Matias\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### Logs de la Aplicación
```bash
# Logs generales de la app
adb logcat -s com.cocido.ramfapp.debug

# Logs específicos de Maps
adb logcat | grep -E "(MapActivity|GoogleMap|Maps)"

# Logs de datos del widget
adb logcat -s WidgetData

# Limpiar logs
adb logcat -c
```

### Build y Deploy
```bash
# Compilar y instalar
./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk

# Ejecutar app
adb shell monkey -p com.cocido.ramfapp.debug -c android.intent.category.LAUNCHER 1
```

---

**Documentación generada el 01 de Octubre de 2025**
**Todos los cambios han sido probados y verificados en el entorno de desarrollo**