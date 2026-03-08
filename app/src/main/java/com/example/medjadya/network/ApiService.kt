package com.example.medjadya.network

import com.example.medjadya.model.Instruction
import com.example.medjadya.model.Medication
import com.example.medjadya.model.Schedule
import com.example.medjadya.model.MedLog
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

// This interface is largely replaced by AppApiService.kt
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

    @GET("logs/{medId}")
    suspend fun getLogs(
        @Path("medId") medId: Int
    ): List<MedLog>
}
