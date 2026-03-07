package com.example.medjadya.data

import com.example.medjadya.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MedicationApi {
    // --- Auth ---
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // --- Medication ---
    @GET("api/meds")
    suspend fun getAllMeds(): Response<List<Medication>>

    @GET("api/meds/{id}")
    suspend fun getMedById(@Path("id") id: Int): Response<Medication>

    @GET("api/meds/user/{userId}")
    suspend fun getMedsByUserId(@Path("userId") userId: Int): Response<List<Medication>>

    // --- Logs ---
    @POST("api/logs/{medId}")
    suspend fun logMedication(
        @Path("medId") medId: Int,
        @Body request: LogStatusRequest
    ): Response<LogResponse>

    @PUT("api/logs/{logId}")
    suspend fun updateMedLog(
        @Path("logId") logId: Int,
        @Body request: LogStatusRequest
    ): Response<Unit>

    @GET("api/logs/{medId}")
    suspend fun getLogsByMedId(@Path("medId") medId: Int): Response<List<Medication>>

    // --- Schedules ---
    @GET("api/schedules/{medId}")
    suspend fun getSchedulesByMedId(@Path("medId") medId: Int): Response<List<Schedule>>

    @POST("api/schedules/{medId}")
    suspend fun createSchedule(
        @Path("medId") medId: Int,
        @Body schedule: Schedule
    ): Response<Unit>

    @DELETE("api/schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: Int): Response<Unit>
}

data class LogStatusRequest(
    val status: String, // 'taken' or 'missed'
    val taken: String? = null // Format: YYYY-MM-DD HH:mm:ss
)

data class LogResponse(
    val message: String,
    val logId: Int?
)
