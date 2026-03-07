package com.example.medjadya.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val age: Int,
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("message") val message: String? = null
)
