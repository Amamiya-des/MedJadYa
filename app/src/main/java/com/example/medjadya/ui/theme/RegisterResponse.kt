package com.example.medjadya.ui.theme

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName(value = "error") val error: Boolean,
    @SerializedName(value = "message") val message: String
)