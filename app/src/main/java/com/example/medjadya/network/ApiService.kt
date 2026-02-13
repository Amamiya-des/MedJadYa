package com.example.medjadya.network

import com.example.medjadya.model.Medication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface MedicationApiService {
    @GET("medications")
    suspend fun getMedications(): List<Medication>
}

object RetrofitClient {
    // Use 10.0.2.2 to access localhost from Android Emulator
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: MedicationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedicationApiService::class.java)
    }
}
