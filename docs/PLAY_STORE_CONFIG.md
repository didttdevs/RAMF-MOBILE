# Configuración para Play Store - RAMF App

## 1. Información Básica de la App

### 1.1 Detalles de la Aplicación
- **Nombre**: RAMF - Red Agrometeorológica de Formosa
- **Paquete**: com.cocido.ramfapp
- **Versión**: 1.0.0
- **Categoría**: Weather
- **Clasificación de contenido**: Everyone
- **Idioma principal**: Spanish (Argentina)

### 1.2 Descripción Corta
"Accede a datos meteorológicos en tiempo real de toda la provincia de Formosa. Estaciones, gráficos, pronósticos y alertas."

### 1.3 Descripción Completa
```
🌤️ RAMF - Red Agrometeorológica de Formosa

La aplicación oficial de la Red Agrometeorológica de Formosa te brinda acceso a datos meteorológicos en tiempo real de toda la provincia.

📊 CARACTERÍSTICAS PRINCIPALES:

• Estaciones Meteorológicas
  - Más de 20 estaciones distribuidas por Formosa
  - Datos en tiempo real: temperatura, humedad, precipitación
  - Información de viento, presión y radiación solar

• Mapa Interactivo
  - Visualización de estaciones en mapa de Formosa
  - Marcadores con datos actuales
  - Navegación intuitiva por la provincia

• Gráficos y Análisis
  - Gráficos detallados de tendencias
  - Análisis histórico de datos
  - Comparación entre estaciones

• Pronósticos Meteorológicos
  - Imágenes de pronóstico
  - Alertas meteorológicas
  - Información para planificación agrícola

• Reportes y Alertas
  - Creación de reportes de incidencias
  - Notificaciones de condiciones importantes
  - Historial de reportes

• Exportación de Datos
  - Exportar datos a CSV
  - Compartir gráficos como imágenes
  - Datos para análisis externo

🔧 FUNCIONALIDADES TÉCNICAS:

• Modo Offline
  - Descarga de datos para uso sin conexión
  - Sincronización automática
  - Almacenamiento local seguro

• Personalización
  - Configuración de notificaciones
  - Preferencias de visualización
  - Tema claro y oscuro

• Seguridad
  - Autenticación segura
  - Cifrado de datos
  - Protección de privacidad

🌱 PARA QUIÉN ES ESTA APP:

• Productores agrícolas
• Investigadores meteorológicos
• Estudiantes y educadores
• Público general interesado en el clima
• Planificadores urbanos
• Organizaciones ambientales

📱 COMPATIBILIDAD:

• Android 7.0 (API 24) o superior
• Optimizada para tablets y smartphones
• Funciona en modo offline
• Interfaz en español argentino

🏛️ DESARROLLADO POR:

Gobierno de la Provincia de Formosa
Secretaría de Desarrollo Económico
Dirección de Recursos Naturales

Para más información: https://ramf.formosa.gob.ar
```

## 2. Assets Gráficos Requeridos

### 2.1 Iconos de la App
- **Icono principal**: 512x512 px (PNG)
- **Icono adaptativo**: Múltiples tamaños
- **Icono de notificación**: 24x24 px

### 2.2 Screenshots
- **Teléfono**: 1080x1920 px (mínimo 2, máximo 8)
- **Tablet**: 1200x1920 px (mínimo 2, máximo 8)
- **7-inch tablet**: 1200x1920 px (opcional)

### 2.3 Feature Graphic
- **Tamaño**: 1024x500 px
- **Formato**: PNG o JPG
- **Estilo**: Representativo de la app

## 3. Configuración de Permisos

### 3.1 Permisos Declarados
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 3.2 Permisos Opcionales
- **Ubicación**: Para estaciones cercanas
- **Cámara**: Para reportes con fotos
- **Almacenamiento**: Para exportar datos

### 3.3 Clave Google Maps
- **Debug**: paquete `com.cocido.ramfapp.debug` + SHA-1 del keystore `~/.android/debug.keystore`.
- **Release**: paquete `com.cocido.ramfapp` + SHA-1 del keystore `release-keystore.jks` (alias `ramf-app`).
- **Restricciones de API**: `Maps SDK for Android` (sumar otras APIs según uso).
- **Distribución**: copiar `local.properties.example` a `local.properties`, definir `MAPS_API_KEY` o exportarlo como variable de entorno; el build lo inyecta vía `manifestPlaceholders`.

## 4. Configuración de Target SDK

### 4.1 Versiones
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Compile SDK**: 34

### 4.2 Características Requeridas
- **Internet**: Requerido
- **Ubicación**: Opcional
- **Cámara**: Opcional

## 5. Configuración de Contenido

### 5.1 Clasificación de Contenido
- **Violencia**: Ninguna
- **Contenido sexual**: Ninguno
- **Lenguaje**: Ninguno
- **Drogas**: Ninguna
- **Apuestas**: Ninguna

### 5.2 Audiencia Objetivo
- **Edad**: 13+ años
- **Categoría**: Educativa/Científica
- **Idioma**: Español (Argentina)

## 6. Configuración de Precios

### 6.1 Modelo de Negocio
- **Precio**: Gratis
- **Compras in-app**: Ninguna
- **Publicidad**: Ninguna

### 6.2 Disponibilidad
- **Países**: Argentina (principal), otros países de habla hispana
- **Dispositivos**: Teléfonos y tablets
- **Idiomas**: Español

## 7. Configuración de Privacidad

### 7.1 Datos Recopilados
- **Información personal**: Nombre, email
- **Datos de uso**: Análisis de uso de la app
- **Ubicación**: Solo si se otorga permiso
- **Datos de dispositivo**: Modelo, versión de Android

### 7.2 Datos Compartidos
- **Terceros**: Ninguno
- **Fines comerciales**: No
- **Venta de datos**: No

## 8. Configuración de Notificaciones

### 8.1 Tipos de Notificaciones
- **Alertas meteorológicas**: Condiciones importantes
- **Actualizaciones de datos**: Nuevos datos disponibles
- **Recordatorios**: Configuración de la app

### 8.2 Configuración
- **Opcionales**: Todas las notificaciones son opcionales
- **Configuración**: Disponible en la app
- **Frecuencia**: Baja (solo alertas importantes)

## 9. Configuración de Accesibilidad

### 9.1 Características de Accesibilidad
- **Lectores de pantalla**: Compatible
- **Alto contraste**: Soporte
- **Tamaño de fuente**: Escalable
- **Navegación por teclado**: Compatible

### 9.2 Estándares
- **WCAG**: Nivel AA
- **Android Accessibility**: Cumple estándares
- **Testing**: Probado con TalkBack

## 10. Configuración de Testing

### 10.1 Dispositivos de Prueba
- **Teléfonos**: Android 7.0 a 14
- **Tablets**: 7" y 10"
- **Resoluciones**: 720p a 4K
- **Orientaciones**: Portrait y landscape

### 10.2 Casos de Prueba
- **Funcionalidad básica**: Todas las características
- **Modo offline**: Funcionamiento sin conexión
- **Notificaciones**: Envío y recepción
- **Exportación**: CSV e imágenes
- **Mapa**: Interacción y marcadores

## 11. Configuración de Release

### 11.1 Estrategia de Lanzamiento
- **Fase 1**: Release interno (testing)
- **Fase 2**: Release cerrado (beta testers)
- **Fase 3**: Release gradual (5% de usuarios)
- **Fase 4**: Release completo (100% de usuarios)

### 11.2 Monitoreo
- **Crashlytics**: Monitoreo de errores
- **Analytics**: Métricas de uso
- **Feedback**: Sistema de comentarios
- **Soporte**: Canal de soporte técnico

## 12. Configuración de Marketing

### 12.1 Palabras Clave
- **Principales**: clima, meteorología, Formosa, estaciones
- **Secundarias**: agricultura, pronóstico, datos, gráficos
- **Long-tail**: datos meteorológicos Formosa, estaciones clima Argentina

### 12.2 Descripción de Marketing
- **Título**: RAMF - Clima Formosa
- **Subtítulo**: Datos meteorológicos en tiempo real
- **Tags**: #clima #meteorologia #Formosa #agricultura

## 13. Configuración de Soporte

### 13.1 Información de Contacto
- **Email**: soporte@ramf.formosa.gob.ar
- **Teléfono**: +54 370 412-3456
- **Sitio web**: https://ramf.formosa.gob.ar
- **Horarios**: Lun-Vie 8:00-17:00

### 13.2 Recursos de Ayuda
- **FAQ**: Preguntas frecuentes
- **Tutorial**: Guía de uso
- **Videos**: Demostraciones
- **Documentación**: Manual de usuario

---

**Esta configuración debe ser revisada y actualizada antes de cada release en Play Store.**









