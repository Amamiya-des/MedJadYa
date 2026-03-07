package com.example.medjadya.data.model

import com.google.gson.annotations.SerializedName

data class LoginReq(
    val email: String,
    val password: String
)
data class UserDto(
    val idUser: Int = 0,
    val name: String = "",
    val age: Int? = null,
    val email: String = ""
)

data class ScheduleDto(val idSchedules: Int, val time: String?, val type: String?)
data class InstructionDto(
  val idinstruction: Int,
  val amount: String?,
  val instructions: String?,
  val start_date: String?,
  val stop_date: String?,
  val quantity: Int?,
  val remain: Int?
)
data class RefillDto(val idRefillAlerts: Int, val threshold: Int?, val notified: String?)
data class MedDto(
    @SerializedName("idmed") val idMed: Int,
    @SerializedName("name") val medName: String?,
    val instruction: InstructionDto? = null
)

data class TakeReq(val takenAt: String? = null, val status: String = "taken", val amountInt: Int = 1)
data class SimpleRes(val ok: Boolean)

data class LoginRes(
    val ok: Boolean,
    val user: UserDto
)
