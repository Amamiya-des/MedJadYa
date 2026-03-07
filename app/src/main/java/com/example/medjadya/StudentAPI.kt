package com.example.medjadya

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StudentAPI {
    // Register
    @POST("register")
    suspend fun registerStudent(
        @Body studentData: RegisterClass
    ): Response<RegisterResponse>

    // Login
    @POST("login")
    suspend fun loginStudent(
        @Body loginData: Map<String, String>
    ): Response<LoginClass>

    // Search/Profile
    @GET("search/{id}")
    suspend fun getStudentProfile(
        @Path("id") id: String
    ): Response<ProfileClass>

    // Update Profile
    @PUT("updateStd/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body userData: Map<String, String>
    ): Response<RegisterResponse> // ใช้ RegisterResponse เพราะโครงสร้าง error/message เหมือนกัน
}
