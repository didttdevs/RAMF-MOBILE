package com.cocido.ramfapp.models

data class RegisterRequest(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String
)