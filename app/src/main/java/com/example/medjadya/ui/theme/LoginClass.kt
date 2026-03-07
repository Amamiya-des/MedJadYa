package com.example.medjadya.ui.theme

import com.google.gson.annotations.SerializedName

data class LoginClass(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("idUser") val idUser: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String?
)
