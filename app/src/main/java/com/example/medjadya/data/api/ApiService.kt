package com.example.medjadya.data.api

import com.example.medjadya.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginReq): LoginRes

    @GET("meds")
    suspend fun getMeds(@Query("userId") userId: Int): List<MedDto>

  @POST("meds/{id}/take")
  suspend fun takeMed(@Path("id") medId: Int, @Body req: TakeReq): SimpleRes

  @POST("refill/{medId}")
  suspend fun refill(@Path("medId") medId: Int): SimpleRes

  @GET("api/refill-alerts")
  suspend fun getRefillAlerts(): List<RefillItemUi>

  @PATCH("api/medicines/{id}/refill")
  suspend fun refillMedicine(@Path("id") id: Int)
}
