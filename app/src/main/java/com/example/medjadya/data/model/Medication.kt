package com.example.medjadya.data.model

import com.google.gson.annotations.SerializedName

enum class TimeSlot {
    MORNING, LUNCH, EVENING, BEFORE_BED
}

data class Medication(
    @SerializedName("idmed") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("form") val form: String?,
    @SerializedName("idUser") val idUser: Int?,
    
    @SerializedName("time") val time: String? = null,
    @SerializedName("hour") val hour: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("day") val day: String? = null,
    
    @SerializedName("status") val status: String? = null,
    @SerializedName("idmedLogs") val logId: Int? = null,
    
    @SerializedName("dosage") val dosage: String? = "1",
    @SerializedName("instruction") val instruction: String? = "หลังอาหาร",
    @SerializedName("is_urgent") val isUrgentInt: Int? = 0
) {
    val isTaken: Boolean get() {
        val s = status?.toString()?.trim()?.lowercase() ?: return false
        // Check for all possible success indicators from DB
        return s == "taken" || 
               s == "success" || 
               s == "completed" || 
               s == "done" || 
               s == "1" || 
               s == "true" || 
               s == "yes" ||
               s.contains("taken") || 
               s.contains("success")
    }

    val isMissedStatus: Boolean get() {
        val s = status?.toString()?.trim()?.lowercase() ?: return false
        return s == "missed" || s == "0" || s == "fail" || s == "false" || s == "no"
    }

    val isUrgent: Boolean get() = isUrgentInt == 1
}

data class Schedule(
    @SerializedName("idSchedules") val id: Int,
    @SerializedName("time") val time: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("hour") val hour: String?,
    @SerializedName("med_idmed") val medId: Int,
    @SerializedName("create_time") val createTime: String?,
    @SerializedName("update_time") val updateTime: String?,
    @SerializedName("delete_time") val deleteTime: String?
)
