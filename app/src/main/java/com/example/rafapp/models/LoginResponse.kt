package com.example.rafapp.models

data class LoginResponse(
    val token: String,
    val user: User // Aquí almacenamos la información del usuario
)
