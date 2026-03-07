package com.example.medjadya

import com.google.gson.annotations.SerializedName

data class ProfileClass(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("idUser") val idUser: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String?
)
