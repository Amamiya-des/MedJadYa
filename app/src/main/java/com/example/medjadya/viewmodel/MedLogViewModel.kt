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

    fun fetchMedLogs(medId: Int) {
        viewModelScope.launch {
            try {
                val logs = RetrofitClient.instance.getLogs(medId)

                if (logs.isNotEmpty()) {
                    // 2. เก็บข้อมูลลงใน allLogsList เพื่อใช้กรองในภายหลัง
                    allLogsList = logs

                    // 3. เรียก filterData เพื่อคำนวณสถิติเริ่มต้น (เช่น สัปดาห์ หรือ ทั้งหมด)
                    filterData("ทั้งหมด", allLogsList)
                } else {
                    Log.d("API_DEBUG", "No data found for medId: $medId")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error fetching data: ${e.message}")
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

        // 1. จัดกลุ่มตามวันที่ 10 หลักแรก (yyyy-MM-dd)
        val grouped = logs.groupBy { it.taken.substring(0, 10) }

        val summaries = grouped.map { (dateString, dayLogs) ->
            val date = inputFormat.parse(dateString)
            val displayDateText = if (date != null) outputFormat.format(date) else "วันที่ไม่ถูกต้อง"

            // สร้างรายการยาแยกตามมื้อ
            val mealsList = listOf("เช้า", "กลางวัน", "เย็น", "ก่อนนอน").map { mealName ->
                val medsInMeal = dayLogs.filter { log ->
                    val hour = log.taken.substring(11, 13).toInt()
                    when (mealName) {
                        "เช้า" -> hour in 5..9
                        "กลางวัน" -> hour in 11..13
                        "เย็น" -> hour in 16..19
                        "ก่อนนอน" -> hour >= 20 || hour < 5
                        else -> false
                    }
                }.map { log ->
                    val statusThai = if (log.status == "taken") "ทานแล้ว" else "ไม่ได้ทาน"
                    if (log.status == "taken") {
                        // แสดงเวลาเฉพาะกรณีที่ทานแล้ว
                        val timeOnly = log.taken.substring(11, 16)
                        "${log.med_name} ($statusThai) - $timeOnly น."
                    } else {
                        "${log.med_name} ($statusThai)"
                    }
                }
                MedByMeal(mealName = mealName, meds = medsInMeal)
            }

            val takenCount = dayLogs.count { it.status == "taken" }
            val total = dayLogs.size
            val missedCount = total - takenCount

            DailySummary(
                displayDate = displayDateText,
                takenCount = takenCount,
                totalCount = total,
                progress = if (total > 0) takenCount.toFloat() / total else 0f,
                statusText = if (takenCount == total) "ทานครบ" else "ไม่ได้ทาน $missedCount ครั้ง",
                meals = mealsList, // ใส่ข้อมูลมื้ออาหารที่คำนวณไว้
                dateKey = dateString // เก็บค่า yyyy-MM-dd ไว้สำหรับ Sorting
            )
        }
            // 2. เรียงลำดับจากวันที่ล่าสุดขึ้นก่อน (Descending)
            .sortedByDescending { it.dateKey }

        dailySummaries.clear()
        dailySummaries.addAll(summaries)
    }
}