package com.example.medjadya.model

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// --- Auth Models ---
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val age: Int, val email: String, val password: String)
data class AuthResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("idUser") val idUser: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("password") val password: String? // Added to support showing password directly
)

// --- User Profile Models ---
data class UserData(
    @SerializedName("idUser") val idUser: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("email") val email: String?
)

// --- Medication Models ---
enum class TimeSlot { MORNING, LUNCH, EVENING, BEFORE_BED, HOURLY }

data class Medication(
    @SerializedName("idmed", alternate = ["id", "idMed", "idmeds", "med_id", "id_med"]) 
    val id: Int?,
    val name: String?,
    val form: String?,
    val instruction: Instruction?,
    @SerializedName("schedules", alternate = ["Schedules"]) 
    val schedules: List<Schedule>? = emptyList(),
    val logs: List<MedLog>? = emptyList()
) {
    val dosage: String? get() = instruction?.amount
    val remainingCount: Int? get() = instruction?.remain
    
    fun isTakenInSlot(slot: TimeSlot): Boolean {
        if (logs.isNullOrEmpty()) return false
        
        val now = ZonedDateTime.now()
        val logsToday = logs.filter { log ->
            if (log.status != "taken") return@filter false
            val takenAtStr = log.taken ?: return@filter false
            
            val logTimeLocal: ZonedDateTime = try {
                Instant.parse(takenAtStr).atZone(now.zone) 
            } catch (e: Exception) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    LocalDateTime.parse(takenAtStr, formatter).atZone(now.zone)
                } catch (e2: Exception) {
                    return@filter false
                }
            }
            logTimeLocal.toLocalDate().isEqual(now.toLocalDate())
        }.sortedBy { it.taken }

        if (logsToday.isEmpty()) return false
        if (slot == TimeSlot.HOURLY) return true 

        val allSchedules = schedules?.filter { sched ->
            val type = sched.type?.lowercase()
            !(type == "hourly" || (sched.time == null && sched.hour != null))
        }?.sortedBy { it.time ?: it.hour ?: "00:00" } ?: emptyList()
        
        val schedulesInSlot = allSchedules.filter { sched ->
            val timeStr = sched.time ?: sched.hour ?: return@filter false
            val hour = try {
                timeStr.substringBefore(':').trim().toInt()
            } catch (e: Exception) { -1 }

            when (slot) {
                TimeSlot.MORNING -> hour in 5..10
                TimeSlot.LUNCH -> hour in 11..14
                TimeSlot.EVENING -> hour in 15..19
                TimeSlot.BEFORE_BED -> hour in 20..23 || hour in 0..4
                else -> false
            }
        }
        
        if (schedulesInSlot.isEmpty()) return false
        
        return schedulesInSlot.any { targetSched ->
            val schedIndex = allSchedules.indexOf(targetSched)
            logsToday.size > schedIndex
        }
    }
}

data class Instruction(
    @SerializedName("idinstruction", alternate = ["id", "idInstruction"])
    val idinstruction: Int?,
    val amount: String?,
    val instructions: String?,
    val quantity: Int?,
    val remain: Int?,
    val start_date: String?,
    val stop_date: String?
)

data class Schedule(
    @SerializedName("idSchedules", alternate = ["id", "idSchedule", "id_schedules"])
    val idSchedules: Int?,
    val time: String?,
    val type: String?,
    val hour: String?,
    val day: String?
)

// --- Medication Request/Response Models ---
data class MedRequest(val name: String, val form: String)

data class MedResponse(
    val message: String? = null,
    @SerializedName(
        "idmed",
        alternate = ["id", "idMed", "idmeds", "insertId", "id_med", "medId", "insertedId", "newId", "id_medication", "medication_id"]
    )
    val idmed: Int? = null,
    val data: MedIdData? = null
)

data class MedIdData(
    @SerializedName(
        "idmed",
        alternate = ["id", "idMed", "idmeds", "insertId", "id_med", "medId", "insertedId", "newId"]
    )
    val idmed: Int? = null
)

data class InstructionRequest(
    val amount: String,
    val instructions: String,
    val start_date: String,
    val stop_date: String,
    val quantity: Int,
    val form: String,
    @SerializedName("med_idmed", alternate = ["medId", "med_id", "idmed", "id_med"])
    val medId: Int? = null
)

data class UpdateInstructionRequest(
    val amount: String?,
    val instructions: String?,
    val quantity: Int?,
    val remain: Int?,
    val start_date: String?,
    val stop_date: String?
)

data class ScheduleRequest(
    val time: String?,
    val type: String?,
    val hour: String?,
    val day: String? = "Daily",
    @SerializedName("med_idmed", alternate = ["medId", "med_id", "idmed", "id_med"])
    val medId: Int? = null
)

// --- Logs & Summaries ---
data class MedLog(
    @SerializedName("idmedLogs") val idmedLogs: Int,
    @SerializedName("taken", alternate = ["taken_at"]) val taken: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("med_idmed") val medIdMed: Int,
    val medName: String?
)

data class LogStatusRequest(
    val status: String,
    val taken: String? = null,
    @SerializedName("taken_at") val takenAt: String? = null
)

data class LogResponse(val message: String, val logId: Int? = null)

data class MedByMeal(val mealName: String, val meds: List<String?> = emptyList())

data class DailySummary(
    val displayDate: String?,
    val takenCount: Int,
    val totalCount: Int,
    val progress: Float,
    val statusText: String,
    val meals: List<MedByMeal>,
    val dateKey: String?
)
