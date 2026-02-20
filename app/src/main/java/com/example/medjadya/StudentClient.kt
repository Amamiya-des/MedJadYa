package com.example.medjadya

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StudentClient {
//    private const val BASE_URL = "http://192.168.1.109:3000"
    private const val BASE_URL = "http://10.153.58.99:3000"

    val studentAPI: StudentAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudentAPI::class.java)
    }
}