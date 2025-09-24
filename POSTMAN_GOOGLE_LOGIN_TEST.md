# 🚀 Pruebas de Google Login en Postman

## 📋 Información del Endpoint

### Configuración Base
- **Base URL**: `https://ramf.formosa.gob.ar/api/http/`
- **Endpoint**: `POST /auth/login/google`
- **Content-Type**: `application/json`

## 🔧 Configuración en Postman

### 1. **Crear Nueva Request**
- **Method**: `POST`
- **URL**: `https://ramf.formosa.gob.ar/api/http/auth/login/google`

### 2. **Headers**
```
Content-Type: application/json
Accept: application/json
```

### 3. **Body (JSON)**

#### **Opción A: Con id_token (si el backend lo espera)**
```json
{
    "email": "usuario@gmail.com",
    "name": "Juan",
    "lastName": "Pérez",
    "avatar": "https://lh3.googleusercontent.com/a/ACg8ocJ...",
    "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### **Opción B: Con idToken (configuración actual)**
```json
{
    "email": "usuario@gmail.com",
    "name": "Juan",
    "lastName": "Pérez",
    "avatar": "https://lh3.googleusercontent.com/a/ACg8ocJ...",
    "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 📝 Ejemplos de Datos de Prueba

### **Usuario de Prueba 1**
```json
{
    "email": "test.user@gmail.com",
    "name": "Test",
    "lastName": "User",
    "avatar": "https://lh3.googleusercontent.com/a/test123",
    "google_id": "1234567890123456789"
}
```

### **Usuario de Prueba 2**
```json
{
    "email": "matias.ramf@gmail.com",
    "name": "Matias",
    "lastName": "RAF",
    "avatar": "https://lh3.googleusercontent.com/a/matias123",
    "google_id": "9876543210987654321"
}
```

## 🔍 Respuestas Esperadas

### **Respuesta Exitosa (200 OK)**
```json
{
    "success": true,
    "data": {
        "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "token_type": "Bearer",
        "expires_in": 3600000,
        "refresh_token": "def50200...",
        "user": {
            "id": "user123",
            "first_name": "Juan",
            "last_name": "Pérez",
            "email": "usuario@gmail.com",
            "avatar": "https://lh3.googleusercontent.com/a/ACg8ocJ...",
            "role": "user",
            "is_active": true,
            "created_at": "2025-01-24T10:00:00.000Z",
            "updated_at": "2025-01-24T10:00:00.000Z",
            "permissions": ["read", "write"]
        }
    },
    "message": "Login exitoso con Google"
}
```

### **Error de Validación (400 Bad Request)**
```json
{
    "success": false,
    "message": "Datos de Google inválidos",
    "error": {
        "code": "validation_error",
        "message": "El campo email es requerido",
        "details": {
            "email": ["El campo email es requerido"]
        }
    }
}
```

### **Error de Usuario No Encontrado (404 Not Found)**
```json
{
    "success": false,
    "message": "Usuario no encontrado",
    "error": {
        "code": "user_not_found",
        "message": "No existe un usuario con ese email de Google"
    }
}
```

### **Error de Servidor (500 Internal Server Error)**
```json
{
    "success": false,
    "message": "Error interno del servidor",
    "error": {
        "code": "server_error",
        "message": "Error procesando autenticación con Google"
    }
}
```

## 🧪 Casos de Prueba

### **Test 1: Login Exitoso**
1. Usar datos válidos de Google
2. Verificar respuesta 200 OK
3. Verificar que `access_token` esté presente
4. Verificar datos del usuario

### **Test 2: Email Faltante**
```json
{
    "name": "Juan",
    "lastName": "Pérez",
    "google_id": "1234567890123456789"
}
```
- Esperar error 400 con mensaje de validación

### **Test 3: Datos Incompletos**
```json
{
    "email": "test@gmail.com"
}
```
- Verificar qué campos son obligatorios

### **Test 4: Usuario No Existente**
- Usar email de Google que no esté registrado
- Verificar respuesta del backend

## 📊 Variables de Entorno en Postman

### **Crear Variables**
```
base_url: https://ramf.formosa.gob.ar/api/http/
auth_endpoint: auth/google
```

### **Usar Variables**
- **URL**: `{{base_url}}{{auth_endpoint}}`

## 🔐 Headers Adicionales (si es necesario)

### **Para Endpoints Protegidos**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
X-Requested-With: XMLHttpRequest
```

## 📱 Datos de la Aplicación Android

### **Datos que Envía la App**
```json
{
    "email": "usuario@gmail.com",
    "name": "Nombre",
    "lastName": "Apellido", 
    "avatar": "https://lh3.googleusercontent.com/a/...",
    "google_id": "1234567890123456789"
}
```

### **Logs de Debug**
Para ver exactamente qué datos envía la app, revisa los logs de Android:
```
performGoogleLogin: Starting Google login for email: usuario@gmail.com
```

## 🚀 Próximos Pasos

1. **Probar endpoint** con datos de prueba
2. **Verificar respuesta** del backend
3. **Ajustar formato** si es necesario
4. **Confirmar funcionamiento** en la app

¡Usa esta configuración para probar el endpoint de Google Login! 🎉
