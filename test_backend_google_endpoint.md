# 🧪 Prueba del Endpoint Google Login

## 📋 **Objetivo**
Determinar exactamente qué formato espera el backend en `/auth/login/google`

## 🔍 **Pruebas a Realizar**

### **Prueba 1: Con idToken (formato actual de la app)**
```bash
curl -X POST https://ramf.formosa.gob.ar/api/http/auth/login/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com",
    "name": "Test",
    "lastName": "User",
    "avatar": "https://example.com/avatar.jpg",
    "idToken": "fake_token_123"
  }'
```

### **Prueba 2: Con id_token (formato alternativo)**
```bash
curl -X POST https://ramf.formosa.gob.ar/api/http/auth/login/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com",
    "name": "Test",
    "lastName": "User",
    "avatar": "https://example.com/avatar.jpg",
    "id_token": "fake_token_123"
  }'
```

### **Prueba 3: Con google_id (formato de ejemplos)**
```bash
curl -X POST https://ramf.formosa.gob.ar/api/http/auth/login/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com",
    "name": "Test",
    "lastName": "User",
    "avatar": "https://example.com/avatar.jpg",
    "google_id": "1234567890123456789"
  }'
```

### **Prueba 4: Solo con email (mínimo requerido)**
```bash
curl -X POST https://ramf.formosa.gob.ar/api/http/auth/login/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com"
  }'
```

## 📊 **Análisis de Respuestas**

### **Respuesta Exitosa (200)**
- ✅ Formato correcto
- ✅ Backend procesa el request

### **Error de Validación (400)**
- ❌ Formato incorrecto
- 📝 Mensaje indica qué campo falta o es inválido

### **Error de Servidor (500)**
- ❌ Error interno
- 🔧 Problema de configuración

## 🎯 **Resultado Esperado**
Una vez que identifiquemos el formato correcto, actualizaremos el código de la app para que coincida exactamente con lo que espera el backend.

## 📱 **Próximo Paso**
Ejecutar estas pruebas y ajustar el código de `LoginActivity.kt` según el formato que funcione.
