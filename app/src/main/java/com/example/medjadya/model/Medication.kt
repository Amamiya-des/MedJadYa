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

data class MedLog(
    val idmedLogs: Int,
    val taken: String,      // เช่น "2026-02-10 08:05:00"
    val status: String,     // "taken" หรือ "missed"
    val med_idmed: Int,
    val med_name: String    // เพิ่มฟิลด์นี้เพื่อรับค่าจาก SQL ที่เรา JOIN ไว้
)

data class MedByMeal(
    val mealName: String,
    val meds: List<String> = emptyList()
)

data class DailySummary(
    val displayDate: String,
    val takenCount: Int,
    val totalCount: Int,
    val progress: Float,
    val statusText: String,
    val meals: List<MedByMeal>,
    val dateKey: String
)

