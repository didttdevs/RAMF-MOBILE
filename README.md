# 📱 RAF App - Red Agrometeorológica de Formosa

Aplicación móvil Android para el monitoreo de estaciones meteorológicas de la Red Agrometeorológica de Formosa (RAMF).

## 📋 Descripción

**RAF App** permite visualizar datos meteorológicos en tiempo real, históricos y pronósticos de las estaciones meteorológicas distribuidas en la provincia de Formosa, Argentina.

## 🚀 Características

- **Datos en Tiempo Real**: Visualización de temperatura, humedad, presión, viento y precipitaciones
- **Gráficos Históricos**: Análisis de datos históricos con gráficos interactivos
- **Múltiples Estaciones**: Acceso a todas las estaciones de la red RAMF
- **Autenticación Google**: Login seguro con cuenta de Google
- **Offline First**: Caché de datos para acceso sin conexión
- **Material Design**: Interfaz moderna y intuitiva

## 🛠️ Tecnologías

- **Lenguaje**: Kotlin
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit2 + OkHttp3
- **Async**: Kotlin Coroutines
- **UI**: Android Views + LiveData
- **Security**: EncryptedSharedPreferences
- **Auth**: Google Sign-In + JWT

## 📦 Requisitos

- **Android Studio**: Hedgehog | 2023.1.1 o superior
- **Kotlin**: 1.9.0 o superior
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **JDK**: 17

## 🔧 Configuración del Proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/Matias-sh/RAF-app.git
cd RAF-app
```

### 2. Configurar Google Services

1. Descargar `google-services.json` desde [Firebase Console](https://console.firebase.google.com/)
2. Colocar el archivo en la carpeta `app/`
3. Verificar que el `package_name` sea `com.cocido.ramfapp`

### 3. Compilar el proyecto

```bash
./gradlew build
```

### 4. Ejecutar la aplicación

```bash
./gradlew installDebug
```

## 📱 Estructura del Proyecto

```
com.cocido.ramfapp/
├── ui/                     # Activities y Fragments
│   ├── activities/
│   └── fragments/
├── viewmodels/            # ViewModels con lógica de negocio
├── models/                # Modelos de datos
├── network/               # Servicios de red (Retrofit)
├── repository/            # Repositorios de datos
└── utils/                 # Utilidades y helpers
```

## 🔐 Autenticación

La aplicación soporta dos métodos de autenticación:

1. **Email/Password**: Login tradicional con credenciales
2. **Google Sign-In**: Autenticación mediante cuenta de Google

Los tokens JWT se almacenan de forma segura usando `EncryptedSharedPreferences`.

## 🌐 API

**Base URL**: `https://ramf.formosa.gob.ar/api/http/`

**Documentación**: [API Docs](https://ramf.formosa.gob.ar/api/http/docs/)

### Endpoints principales:

- `GET /stations` - Lista de estaciones meteorológicas
- `GET /stations-measurement/widget/{stationName}` - Datos del widget
- `POST /auth/login` - Login con email/password
- `POST /auth/login/google` - Login con Google

## 🏗️ Build Variants

### Debug
```bash
./gradlew assembleDebug
```
- Package: `com.cocido.ramfapp.debug`
- Depuración habilitada
- Logs detallados

### Release
```bash
./gradlew assembleRelease
```
- Package: `com.cocido.ramfapp`
- ProGuard habilitado
- Ofuscación de código

## 📄 Documentación Técnica

Para documentación técnica detallada, consultar:
- [Documentación Técnica Completa](DOCUMENTACION_TECNICA_RAF_APP.md)

## 🤝 Contribuir

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📝 Versionado

Usamos [SemVer](http://semver.org/) para el versionado.

**Versión actual**: 1.5.0

## 👥 Autores

- **Equipo de Desarrollo RAF App**

## 📄 Licencia

Este proyecto es propiedad del Gobierno de la Provincia de Formosa.

## 📧 Contacto

Para consultas o soporte:
- Email: contacto@ramf.formosa.gob.ar
- Web: [https://ramf.formosa.gob.ar](https://ramf.formosa.gob.ar)

---

**Desarrollado con ❤️ en Formosa, Argentina**

