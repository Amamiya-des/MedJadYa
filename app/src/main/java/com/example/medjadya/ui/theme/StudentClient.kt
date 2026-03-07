package com.example.medjadya.ui.theme

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StudentClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val studentAPI: StudentAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudentAPI::class.java)
    }
}