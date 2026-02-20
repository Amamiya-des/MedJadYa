package com.example.medjadya

import com.google.gson.annotations.SerializedName

data class RegisterClass(
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String
)
