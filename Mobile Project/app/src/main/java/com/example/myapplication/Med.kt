package com.example.myapplication

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Med(
    @SerializedName("idmed")
    @Expose
    val idmed: Int,

    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("form")
    @Expose
    val form: String,

    @SerializedName("user_idUser")
    @Expose
    val user_idUser: Int,

    @SerializedName("quantity")
    @Expose
    val quantity: Int? = 0,

    @SerializedName("remain")
    @Expose
    val remain: Int? = 0
)
