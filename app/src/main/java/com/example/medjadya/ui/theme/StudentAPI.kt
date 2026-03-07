package com.example.medjadya.ui.theme

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudentAPI {
    // Register
    @POST(value = "register")
    suspend fun registerStudent(
        @Body studentData: RegisterClass
    ): Response<RegisterResponse>

    // Login
    @POST(value = "login")
    suspend fun loginStudent(
        @Body loginData: Map<String, String>
    ): Response<LoginClass>

    // Search/Profile
    @GET(value = "search/{id}")
    suspend fun getstudentProfile(
        @Path(value = "id") id: String
    ): Response<ProfileClass>
}