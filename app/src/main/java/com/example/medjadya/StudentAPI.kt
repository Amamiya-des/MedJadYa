package com.example.medjadya

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
