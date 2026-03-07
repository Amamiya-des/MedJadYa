package com.example.medjadya

import com.google.gson.annotations.SerializedName

data class LoginClass(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("idUser") val idUser: Int?,
    @SerializedName("name") val name: String?
)
