package com.example.medjadya.ui.theme

import com.google.gson.annotations.SerializedName

data class RegisterClass(
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
