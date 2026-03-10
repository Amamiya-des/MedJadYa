package com.example.medjadya.network

import com.example.medjadya.model.*
import retrofit2.Response
import retrofit2.http.*

interface AppApiService {
    // --- Auth & User ---
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/users/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("api/users/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("api/users/search/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<UserData>

    @PUT("api/users/updateStd/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body userData: Map<String, String>
    ): Response<AuthResponse>

    // --- Medications ---
    @GET("api/meds")
    suspend fun getAllMeds(): Response<List<Medication>>

    @GET("api/meds/user/{userId}")
    suspend fun getMedsByUserId(@Path("userId") userId: Int): Response<List<Medication>>

    @POST("api/meds")
    suspend fun insertMed(@Body request: MedRequest): Response<MedResponse>

    @DELETE("api/meds/{id}")
    suspend fun deleteMed(@Path("id") id: Int): Response<Unit>

    // --- Instructions ---
    @GET("api/instructions/{medId}")
    suspend fun getInstructions(@Path("medId") medId: Int): List<Instruction>

    @POST("api/instructions/{medId}")
    suspend fun insertInstruction(
        @Path("medId") medId: Int,
        @Body request: InstructionRequest
    ): Response<Unit>

    @PUT("api/instructions/{id}")
    suspend fun updateInstruction(
        @Path("id") id: Int,
        @Body request: UpdateInstructionRequest
    ): Response<Unit>

    // --- Schedules ---
    @GET("api/schedules/{medId}")
    suspend fun getSchedules(@Path("medId") medId: Int): List<Schedule>

    @POST("api/schedules/{medId}")
    suspend fun insertSchedule(
        @Path("medId") medId: Int,
        @Body request: ScheduleRequest
    ): Response<Unit>

    // --- Logs ---
    @POST("api/logs/{medId}")
    suspend fun logMedication(
        @Path("medId") medId: Int,
        @Body request: LogStatusRequest
    ): Response<LogResponse>

    @GET("api/logs/{medId}")
    suspend fun getLogsByMedId(@Path("medId") medId: Int): List<MedLog>
}
