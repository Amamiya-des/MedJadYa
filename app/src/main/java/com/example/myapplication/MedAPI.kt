package com.example.myapplication

import retrofit2.Call
import retrofit2.http.*

interface MedAPI {
    @GET("api/meds")
    fun getAllMed(): Call<List<Med>>

    @POST("api/meds")
    fun insertMed(@Body body: MedRequest): Call<MedResponse>

    @POST("api/instructions/{medId}")
    fun insertInstruction(
        @Path("medId") medId: Int,
        @Body body: InstructionRequest
    ): Call<Void>

    @POST("api/schedules/{medId}")
    fun insertSchedule(
        @Path("medId") medId: Int,
        @Body body: ScheduleRequest
    ): Call<Void>

    @DELETE("api/meds/{id}")
    fun deleteMed(@Path("id") id: Int): Call<Void>
}

data class MedRequest(
    val name: String,
    val form: String
)

data class MedResponse(
    val message: String,
    val idmed: Int? = null
)

data class InstructionRequest(
    val amount: String,
    val instructions: String,
    val start_date: String,
    val stop_date: String,
    val quantity: Int,
    val form: String
)

data class ScheduleRequest(
    val time: String? = null,
    val type: String? = null,
    val hour: String? = null
)
