package com.example.medjadya.network

import com.example.medjadya.model.MedLog
import com.example.medjadya.model.Medication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MedicationApiService {
    @GET("medications")
    suspend fun getMedications(): List<Medication>


    @GET("api/logs/{medId}") // ตรวจสอบว่ามี /api/ นำหน้าตามที่ตั้งใน server.js หรือไม่
    suspend fun getLogs(@Path("medId") medId: Int): List<MedLog>

}

object RetrofitClient {
    // Use 10.0.2.2 to access localhost from Android Emulator
    private const val BASE_URL = "http://192.168.56.1:3000/"

    val instance: MedicationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedicationApiService::class.java)
    }
}


