# Análisis Exhaustivo: Sistema de Autenticación Android vs Backend vs Frontend

## ✅ COMPARACIÓN COMPLETA

### 1. REGISTRO DE USUARIO

#### Backend (`auth.controller.ts` línea 15-22):
- **Endpoint**: `POST /auth/register`
- **Retorna**: `204 No Content` (sin body)
- **DTO**: `RegisterDto extends CreateUserDto { name, lastName, email, password }`

#### Frontend (`signup-form.tsx` línea 82-88):
- **Envía**: `{ name, lastName, email: email.toLowerCase(), password }`
- ✅ Normaliza email a lowercase

#### Android (`RegisterActivity.kt`):
- ✅ Email normalizado a lowercase (línea 55)
- ✅ Endpoint correcto: `POST /auth/register`
- ✅ Retorna `Response<Unit>` (204 No Content)
- ✅ Validación de contraseña correcta (8 chars, mayúscula, minúscula, número)

**CONCLUSIÓN**: ✅ Android está CORRECTO

---

### 2. LOGIN NORMAL (Email/Password)

#### Backend (`auth.service.ts` línea 52-64):
- **Endpoint**: `POST /auth/login`
- **Retorna**: `202 Accepted` con `AuthResponse { accessToken, refreshToken, user }`
- **Busca usuario**: `usersService.findOneByEmail(loginDto.email.toLocaleLowerCase())`
- ✅ Normaliza email a lowercase antes de buscar

#### Frontend (`signin-form.tsx` + `auth.service.ts`):
- **Envía**: `{ email, password }` (sin normalizar en el form)
- **Retorna**: `202` con `AuthResponse`

#### Android (`LoginActivity.kt`):
- ✅ Email normalizado a lowercase (línea 82)
- ✅ Endpoint correcto: `POST /auth/login`
- ✅ Retorna `Response<LoginResponse>` directamente
- ⚠️ **PROBLEMA**: No verifica que la respuesta sea `202`. El backend retorna `202`, no `200`

**CONCLUSIÓN**: ⚠️ Android necesita verificar código `202` en vez de `response.isSuccessful`

---

### 3. LOGIN CON GOOGLE

#### Backend (`auth.controller.ts` línea 32-38):
```typescript
@Post('login/google')
async googleLogin(@Body('idToken') idToken: string) {
  return await this.authService.loginWithGoogle(idToken);
}
```
- **Espera SOLO**: `{ idToken: string }` en el body
- El backend extrae `email, given_name, family_name, picture, sub` del idToken

#### Frontend (`google-auth-btn.tsx` línea 21):
```typescript
const res = await httpClient.post<AuthResponse>("/auth/login/google", { idToken: token });
```
- ✅ Envía SOLO `{ idToken: token }`

#### Android (`LoginActivity.kt` línea 224-234):
```kotlin
val googleToken = mapOf(
    "email" to (account.email ?: ""),
    "name" to fullName,
    "firstName" to (account.givenName ?: ""),
    "lastName" to (account.familyName ?: ""),
    "avatar" to (account.photoUrl?.toString() ?: ""),
    "google_id" to (account.id ?: ""),
    "idToken" to (account.idToken ?: "")
)
```
- ❌ **PROBLEMA CRÍTICO**: Envía 7 campos cuando el backend solo espera `idToken`
- El backend ignora todos los demás campos y solo usa el `idToken`

**CONCLUSIÓN**: ❌ Android está MAL - debe enviar SOLO `{ idToken }`

---

### 4. CAMBIO DE CONTRASEÑA

#### Backend - DOS endpoints:

**Endpoint 1** (`auth.controller.ts` línea 93-102):
- `PATCH /auth/change-password`
- Requiere `@Auth()` (solo autenticado)
- Body: `ChangePasswordDto { password, newPassword }`
- Retorna: `204 No Content`

**Endpoint 2** (`users.controller.ts` línea 88-95):
- `PATCH /users/change-password`
- Requiere `@Auth('user:edit')` (permiso específico)
- Body: `ChangePasswordDto { password, newPassword }`
- Retorna: `204 No Content`

#### Frontend (`auth.service.ts` línea 71-79):
```typescript
static async changeUserPassword( password: string, newPassword: string, accessToken: string ) {
  const response = await httpClient.patch(`/auth/change-password`, { password, newPassword }, {
    headers: { Authorization: `Bearer ${accessToken}` }
  });
}
```
- ✅ Usa `PATCH /auth/change-password`
- ✅ Envía `{ password, newPassword }`

#### Android (`ProfileRepository.kt` línea 86-100):
```kotlin
val response = userService.changePassword("Bearer $token", changePasswordRequest)
```
- ❌ **PROBLEMA**: Usa `UserService.changePassword` que es `PATCH /users/change-password`
- ⚠️ Este endpoint requiere permiso `user:edit`, mientras que `/auth/change-password` solo requiere autenticación
- El usuario normal podría no tener el permiso `user:edit`

**CONCLUSIÓN**: ❌ Android debería usar `/auth/change-password` en vez de `/users/change-password`

---

### 5. REFRESH TOKEN

#### Backend (`auth.controller.ts` línea 59-64):
```typescript
@Post('refresh-token')
async refreshToken(@Body('refreshToken') refreshToken: string) {
  return this.authService.refreshToken(refreshToken);
}
```
- **Espera**: `{ refreshToken: string }` en el **body**
- Retorna: `202 Accepted` con `AuthResponse`

#### Frontend (`auth.service.ts` línea 39-46):
```typescript
static async refreshToken(refreshToken: string) {
  const response = await httpClient.post<AuthResponse>("/auth/refresh-token", {
    refreshToken,
  });
}
```
- ✅ Envía `{ refreshToken }` en el body

#### Android (`AuthManager.kt` línea 176):
```kotlin
val response = RetrofitClient.authService.refreshToken("Bearer $refreshToken")
```
- ❌ **PROBLEMA**: Envía el refreshToken en el **header** `Authorization: Bearer refreshToken`
- El backend espera el refreshToken en el **body**, no en el header

**CONCLUSIÓN**: ❌ Android está MAL - debe enviar refreshToken en el body

---

## PLAN DE CORRECCIÓN PARA ANDROID

### Corrección 1: Login Normal - Verificar código 202
**Archivo**: `LoginActivity.kt` línea 123
- Cambiar de `response.isSuccessful` a verificar `response.code() == 202`

### Corrección 2: Login con Google - Solo enviar idToken
**Archivo**: `LoginActivity.kt` línea 224-234
- Eliminar todos los campos excepto `idToken`
- Enviar solo: `mapOf("idToken" to (account.idToken ?: ""))`

### Corrección 3: Cambio de Contraseña - Usar endpoint correcto
**Archivo**: `ProfileRepository.kt` línea 86-100
- Cambiar de `userService.changePassword` a `authService.changePassword`
- El endpoint debe ser `PATCH /auth/change-password` en vez de `PATCH /users/change-password`

### Corrección 4: Refresh Token - Enviar en body
**Archivo**: `AuthService.kt` línea 34-35 y `AuthManager.kt` línea 176
- Cambiar `AuthService.refreshToken` para aceptar el token en el body
- Enviar `{ refreshToken: string }` en el body en vez del header
