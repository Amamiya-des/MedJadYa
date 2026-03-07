package com.example.medjadya.ui.theme

import com.google.gson.annotations.SerializedName

// This class represents the nested "data" object in the API response
data class ProfileData(
    @SerializedName("idUser") val idUser: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("create_time") val create_time: String?,
    @SerializedName("update_time") val update_time: String?
)

// This is the main response class for the profile endpoint
data class ProfileClass(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ProfileData?
)
