package com.example.rafapp.network

import com.example.rafapp.models.LoginRequest
import com.example.rafapp.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    
    @POST("auth/logout")
    fun logout(@Header("Authorization") token: String): Call<Void>
    
    @POST("auth/refresh")
    fun refreshToken(@Header("Authorization") token: String): Call<LoginResponse>
    
    @POST("auth/google")
    fun googleLogin(@Body googleToken: Map<String, String>): Call<LoginResponse>
}
