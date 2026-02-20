package com.example.medjadya.model

import com.google.gson.annotations.SerializedName

data class Medication(
    @SerializedName("idmed") val id: Int?,
    val name: String?,
    val form: String?,
    
    // ข้อมูลจากตาราง instruction
    val instruction: Instruction?,
    
    // ข้อมูลจากตาราง Schedules (ใช้ @SerializedName เพื่อความแม่นยำ)
    @SerializedName("schedules", alternate = ["Schedules"]) 
    val schedules: List<Schedule>? = emptyList()
) {
    val dosage: String?
        get() = instruction?.amount
    
    val remainingCount: Int?
        get() = instruction?.remain
}

data class Instruction(
    val idinstruction: Int?,
    val amount: String?,
    val instructions: String?,
    val quantity: Int?,
    val remain: Int?
)

data class Schedule(
    @SerializedName("idSchedules") val idSchedules: Int?,
    @SerializedName("time") val time: String?, // เช่น "08:00:00"
    @SerializedName("type") val type: String?, // เช่น "daily"
    @SerializedName("day") val day: String?    // เช่น "everyday"
)
