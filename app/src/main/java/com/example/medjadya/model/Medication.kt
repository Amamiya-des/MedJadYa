package com.example.medjadya.model

import com.google.gson.annotations.SerializedName

data class Medication(
    @SerializedName("idmed") val id: Int,
    val name: String,
    val form: String,
    @SerializedName("amount") val dosage: String?, // Map instruction.amount to dosage
    @SerializedName("remain") val remainingCount: Int?, // Map instruction.remain
    val schedules: List<Schedule> = emptyList()
)

data class Schedule(
    val idSchedules: Int,
    val time: String, // e.g., "08:00:00"
    val type: String,
    val day: String
)
