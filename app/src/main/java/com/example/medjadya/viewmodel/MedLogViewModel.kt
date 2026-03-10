package com.example.medjadya.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medjadya.model.DailySummary
import com.example.medjadya.model.MedByMeal
import com.example.medjadya.model.MedLog
import com.example.medjadya.network.RetrofitClient
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MedLogViewModel(context: Context) : ViewModel() {
    private val api = RetrofitClient.getApiService(context)

    var dailySummaries = mutableStateListOf<DailySummary>()
    var allLogsList = mutableStateListOf<MedLog>()
    var selectedPeriod = mutableStateOf("ทั้งหมด")

    var overallProgress = mutableStateOf(0f)
    var totalTaken = mutableStateOf(0)
    var totalMissed = mutableStateOf(0)

    fun fetchMedLogs(userId: Int) {
        viewModelScope.launch {
            try {
                val medsResponse = api.getMedsByUserId(userId)
                if (medsResponse.isSuccessful) {
                    val meds = medsResponse.body() ?: emptyList()

                    val allLogs = supervisorScope {
                        meds.map { med ->
                            async {
                                try {
                                    val medId = med.id ?: return@async emptyList<MedLog>()
                                    val logs = api.getLogsByMedId(medId)
                                    logs.map { it.copy(medName = med.name) }
                                } catch (e: Exception) {
                                    emptyList<MedLog>()
                                }
                            }
                        }.awaitAll().flatten()
                    }

                    allLogsList.clear()
                    allLogsList.addAll(allLogs)
                    filterData(selectedPeriod.value, allLogs)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) Log.e("MedLogVM", "${e.message}")
            }
        }
    }

    fun filterData(period: String, allLogs: List<MedLog>) {
        selectedPeriod.value = period
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filteredLogs = when (period) {
            "สัปดาห์" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                allLogs.filter {
                    it.taken != null && sdf.parse(it.taken.substring(0, 10))
                        ?.after(calendar.time) == true
                }
            }

            "เดือน" -> {
                calendar.add(Calendar.MONTH, -1)
                allLogs.filter {
                    it.taken != null && sdf.parse(it.taken.substring(0, 10))
                        ?.after(calendar.time) == true
                }
            }

            else -> allLogs
        }

        // คำนวณสถิติภาพรวม (นับรายเม็ดสำหรับ Dashboard ด้านบน)
        totalTaken.value = filteredLogs.count { it.status == "taken" }
        totalMissed.value = filteredLogs.count { it.status == "missed" }
        val total = filteredLogs.size
        overallProgress.value = if (total > 0) (totalTaken.value.toFloat() / total) * 100 else 0f

        processLogsToDailySummary(filteredLogs)
    }

    private fun processLogsToDailySummary(logs: List<MedLog>) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE ที่ d MMMM", Locale("th"))

        // Group by Date (YYYY-MM-DD)
        val grouped = logs.groupBy { it.taken?.substring(0, 10) ?: "unknown" }

        val summaries = grouped.mapNotNull { (dateString, dayLogs) ->
            if (dateString == "unknown") return@mapNotNull null

            val date = inputFormat.parse(dateString)
            val displayDateText = if (date != null) outputFormat.format(date) else dateString

            var takenMealsCount = 0
            var totalActiveMeals = 0

            val mealsList = listOf("เช้า", "กลางวัน", "เย็น", "ก่อนนอน").map { mealName ->
                val logsInThisMeal = dayLogs.filter { log ->
                    try {
                        val hourRaw = log.taken?.substring(11, 13)?.toInt() ?: 0
                        val hour = (hourRaw + 7) % 24 // ปรับเป็นเวลาไทย

                        when (mealName) {
                            "เช้า" -> hour in 5..10
                            "กลางวัน" -> hour in 11..14
                            "เย็น" -> hour in 15..19
                            "ก่อนนอน" -> hour >= 20 || hour < 5
                            else -> false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }

                if (logsInThisMeal.isNotEmpty()) {
                    totalActiveMeals++
                    // มื้อนั้นจะ "ผ่าน" ก็ต่อเมื่อทุกเม็ดในมื้อเป็น "taken"
                    if (logsInThisMeal.all { it.status == "taken" }) {
                        takenMealsCount++
                    }
                }

                val medsDetail = logsInThisMeal.map { log ->
                    val statusThai = if (log.status == "taken") "ทานแล้ว" else "ไม่ได้ทาน"
                    val hourRaw = log.taken?.substring(11, 13)?.toInt() ?: 0
                    val minute = log.taken?.substring(14, 16) ?: "00"
                    val displayTime = "${((hourRaw + 7) % 24).toString().padStart(2, '0')}:$minute"

                    if (log.status == "taken") "${log.medName} ($statusThai) - $displayTime น."
                    else "${log.medName} ($statusThai)"
                }

                MedByMeal(mealName = mealName, meds = medsDetail)
            }

            val missedMealsCount = totalActiveMeals - takenMealsCount

            DailySummary(
                displayDate = displayDateText,
                takenCount = takenMealsCount,
                totalCount = totalActiveMeals,
                progress = if (totalActiveMeals > 0) takenMealsCount.toFloat() / totalActiveMeals else 0f,
                statusText = if (takenMealsCount == totalActiveMeals) "ทานครบทุกชนิด" else "พลาดการทานยา $missedMealsCount ชนิด",
                meals = mealsList,
                dateKey = dateString
            )
        }.sortedByDescending { it.dateKey }

        dailySummaries.clear()
        dailySummaries.addAll(summaries)
    }
}

class MedLogViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedLogViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}