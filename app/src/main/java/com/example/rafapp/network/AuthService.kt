package com.example.rafapp.network

import com.example.rafapp.models.LoginRequest
import com.example.rafapp.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
