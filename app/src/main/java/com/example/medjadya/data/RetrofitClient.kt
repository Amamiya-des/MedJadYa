package com.example.medjadya.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywiZW1haWwiOiJ0ZXN0QGdtYWlsLmNvbSIsImlhdCI6MTc3Mjg3MTUyNCwiZXhwIjoxNzcyOTU3OTI0fQ.NHeYOgF74s39OjwS4XV9AMjDRkWQ4gbFw3Zw4FY2Ml8"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer $AUTH_TOKEN")
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    val api: MedicationApi = retrofit.create(MedicationApi::class.java)
}
