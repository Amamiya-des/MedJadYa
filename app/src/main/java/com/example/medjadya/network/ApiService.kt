package com.example.medjadya.network

import com.example.medjadya.model.Instruction
import com.example.medjadya.model.Medication
import com.example.medjadya.model.Schedule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MedicationApiService {
    @GET("meds")
    suspend fun getMeds(
        @Header("Authorization") token: String
    ): List<Medication>

    @GET("instructions/{medId}")
    suspend fun getInstructions(
        @Header("Authorization") token: String,
        @Path("medId") medId: Int
    ): List<Instruction>

    @GET("schedules/{medId}")
    suspend fun getSchedules(
        @Header("Authorization") token: String,
        @Path("medId") medId: Int
    ): List<Schedule>
}

object RetrofitClient {
    // Updated to use the IP 10.153.48.100 as requested
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    val instance: MedicationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedicationApiService::class.java)
    }
}
