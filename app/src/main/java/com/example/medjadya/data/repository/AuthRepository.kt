package com.example.medjadya.data.repository

import android.content.Context
import com.example.medjadya.model.*
import com.example.medjadya.network.AppApiService
import com.example.medjadya.network.RetrofitClient
import retrofit2.Response

class AuthRepository(context: Context) {
    private val api = RetrofitClient.getApiService(context)

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun forgotPassword(email: String): Response<AuthResponse> {
        return api.forgotPassword(mapOf("email" to email))
    }

    suspend fun resetPassword(email: String, newPassword: String): Response<AuthResponse> {
        val request = mapOf(
            "email" to email,
            "password" to newPassword // ตรวจสอบกับ Backend ว่า Key ต้องใช้คำว่า "password" หรือ "newPassword"
        )
        return api.resetPassword(request)
    }

    suspend fun getUserProfile(id: String): Response<UserData> {
        return api.getUserProfile(id)
    }

    suspend fun updateProfile(id: String, userData: Map<String, String>): Response<AuthResponse> {
        return api.updateProfile(id, userData)
    }
}
