package com.example.medjadya.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.model.DailySummary
import com.example.medjadya.model.MedByMeal
import com.example.medjadya.model.MedLog
import com.example.medjadya.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MedLogViewModel : ViewModel() {
    var dailySummaries = mutableStateListOf<DailySummary>()

    // 1. เพิ่มตัวแปรสำหรับเก็บข้อมูล Log ทั้งหมดที่ได้จาก API
    var allLogsList = listOf<MedLog>()

    var totalTaken = mutableStateOf(0)
    var totalMissed = mutableStateOf(0)
    var overallProgress = mutableStateOf(0f)
    var selectedPeriod = mutableStateOf("ทั้งหมด")

    fun fetchMedLogs(userId: Int) {
        viewModelScope.launch {
            try {
                val logs = RetrofitClient.instance.getLogsByUser(userId) // เรียก API ตัวใหม่
                if (logs.isNotEmpty()) {
                    allLogsList = logs
                    filterData("ทั้งหมด", allLogsList)
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "${e.message}")
            }
        }
    }

    // ฟังก์ชันสำหรับกรองข้อมูล (ใช้ allLogsList ที่เก็บไว้)
    fun filterData(period: String, allLogs: List<MedLog>) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val filteredLogs = when(period) {
            "สัปดาห์" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val limitDate = calendar.time
                allLogs.filter { log ->
                    val date = sdf.parse(log.taken.substring(0, 10))
                    date != null && date.after(limitDate)
                }
            }
            "เดือน" -> {
                calendar.add(Calendar.MONTH, -1)
                val limitDate = calendar.time
                allLogs.filter { log ->
                    val date = sdf.parse(log.taken.substring(0, 10))
                    date != null && date.after(limitDate)
                }
            }
            else -> allLogs
        }

        // อัปเดตตัวเลขสถิติสำหรับการ์ดด้านบน
        totalTaken.value = filteredLogs.count { it.status == "taken" }
        totalMissed.value = filteredLogs.count { it.status == "missed" }
        val total = filteredLogs.size

        // คำนวณเปอร์เซ็นต์ความสำเร็จ
        overallProgress.value = if (total > 0) (totalTaken.value.toFloat() / total) * 100 else 0f

        processLogsToDailySummary(filteredLogs)
    }

    private fun processLogsToDailySummary(logs: List<MedLog>) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE ที่ d MMMM", Locale("th"))

        val grouped = logs.groupBy { it.taken.substring(0, 10) }

        val summaries = grouped.map { (dateString, dayLogs) ->
            val date = inputFormat.parse(dateString)
            val displayDateText = if (date != null) outputFormat.format(date) else "วันที่ไม่ถูกต้อง"

            var takenMealsCount = 0
            var totalActiveMeals = 0

            val mealsList = listOf("เช้า", "กลางวัน", "เย็น", "ก่อนนอน").map { mealName ->
                // กรองหา Log เฉพาะของมื้อนั้นๆ
                val logsInThisMeal = dayLogs.filter { log ->
                    try {
                        val hourString = log.taken.substring(11, 13).trim()
                        var hour = hourString.toInt()

                        // --- ส่วนปรับแก้ Timezone ---
                        // ถ้าดึงมาแล้วเวลาหายไป 7 ชั่วโมง ให้บวก 7
                        // ถ้าดึงมาแล้วเวลาเกินไป 7 ชั่วโมง ให้ลบ 7
                        // ตัวอย่าง: แก้ให้เป็นเวลาไทย (UTC+7)
                        hour = (hour + 7) % 24

                        android.util.Log.d("CHECK_HOUR", "Adjusted Hour: $hour | Date: ${log.taken}")

                        when (mealName) {
                            "เช้า" -> hour in 5..10      // เลข 7 จะตกที่นี่แล้ว!
                            "กลางวัน" -> hour in 11..14
                            "เย็น" -> hour in 15..19
                            "ก่อนนอน" -> hour >= 20 || hour < 5
                            else -> false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }

                // --- คำนวณรายมื้อ ---
                if (logsInThisMeal.isNotEmpty()) {
                    totalActiveMeals++ // นับเป็นมื้อที่มีการตั้งค่าทานยาไว้

                    // ถ้าทุกตัวในมื้อนี้สถานะเป็น taken ทั้งหมด (ไม่มี missed เลย)
                    val isMealComplete = logsInThisMeal.all { it.status == "taken" }
                    if (isMealComplete) {
                        takenMealsCount++
                    }
                }

                val medsDetail = logsInThisMeal.map { log ->
                    val statusThai = if (log.status == "taken") "ทานแล้ว" else "ไม่ได้ทาน"

                    // --- ปรับเวลาสำหรับการแสดงผลบนหน้าจอ ---
                    val rawHour = log.taken.substring(11, 13).trim().toInt()
                    val rawMinute = log.taken.substring(14, 16)

                    // ปรับชั่วโมงให้เป็นเวลาไทย (+7 หรือตามที่คุณคำนวณไว้)
                    val adjustedHour = (rawHour + 7) % 24

                    // ทำให้อยู่ในรูปแบบ 00:00 (เติมเลข 0 ข้างหน้าถ้าเป็นเลขหลักเดียว)
                    val displayTime = "${adjustedHour.toString().padStart(2, '0')}:$rawMinute"

                    if (log.status == "taken") {
                        // ใช้ displayTime ที่ปรับแล้วแทน substring อันเดิม
                        "${log.med_name} ($statusThai) - $displayTime น."
                    } else {
                        "${log.med_name} ($statusThai)"
                    }
                }

                MedByMeal(mealName = mealName, meds = medsDetail)
            }

            // คำนวณจำนวนมื้อที่พลาด
            val missedMealsCount = totalActiveMeals - takenMealsCount

            DailySummary(
                displayDate = displayDateText,
                takenCount = takenMealsCount, // เปลี่ยนจากจำนวนเม็ดเป็น "จำนวนมื้อที่ทานครบ"
                totalCount = totalActiveMeals, // เปลี่ยนจากจำนวนเม็ดเป็น "จำนวนมื้อทั้งหมด"
                progress = if (totalActiveMeals > 0) takenMealsCount.toFloat() / totalActiveMeals else 0f,
                statusText = if (takenMealsCount == totalActiveMeals) {
                    "ทานครบทุกมื้อ"
                } else {
                    "พลาดการทานยา $missedMealsCount มื้อ"
                },
                meals = mealsList,
                dateKey = dateString
            )
        }.sortedByDescending { it.dateKey }

        dailySummaries.clear()
        dailySummaries.addAll(summaries)
    }
}