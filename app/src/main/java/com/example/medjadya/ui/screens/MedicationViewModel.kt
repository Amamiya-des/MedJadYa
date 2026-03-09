package com.example.medjadya.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.data.LogStatusRequest
import com.example.medjadya.data.RetrofitClient
import com.example.medjadya.data.model.Medication
import com.example.medjadya.data.model.TimeSlot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicationViewModel : ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var pollingJob: Job? = null

    init {
        startRealtimeUpdates()
        startClock()
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                delay(30000)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    fun startRealtimeUpdates() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var isFirst = true
            while (true) {
                performFetch(showLoading = isFirst)
                isFirst = false
                delay(10000)
            }
        }
    }

    fun fetchMedications(showLoading: Boolean = false) {
        viewModelScope.launch {
            performFetch(showLoading)
        }
    }

    private suspend fun performFetch(showLoading: Boolean) {
        if (showLoading) _isLoading.value = true
        try {
            val medsResponse = RetrofitClient.api.getAllMeds()
            if (medsResponse.isSuccessful) {
                val baseMeds = medsResponse.body() ?: emptyList()
                val resultList = mutableListOf<Medication>()

                for (med in baseMeds) {
                    try {
                        val scheduleResponse = RetrofitClient.api.getSchedulesByMedId(med.id)
                        val schedules = if (scheduleResponse.isSuccessful) scheduleResponse.body() ?: emptyList() else emptyList()
                        
                        val logResponse = RetrofitClient.api.getLogsByMedId(med.id)
                        val logs = if (logResponse.isSuccessful) logResponse.body() ?: emptyList() else emptyList()
                        
                        val latestLog = logs.maxByOrNull { it.logId ?: 0 }
                        val finalStatus = latestLog?.status ?: med.status
                        val finalLogId = latestLog?.logId ?: med.logId

                        if (schedules.isEmpty()) {
                            resultList.add(med.copy(status = finalStatus, logId = finalLogId))
                        } else {
                            for (sch in schedules) {
                                resultList.add(
                                    med.copy(
                                        time = sch.time,
                                        hour = sch.hour,
                                        type = sch.type,
                                        status = finalStatus,
                                        logId = finalLogId
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        resultList.add(med)
                    }
                }
                _medications.value = resultList
            }
        } catch (e: Exception) {
            Log.e("MedicationVM", "Fetch Error: ${e.message}")
        } finally {
            if (showLoading) _isLoading.value = false
        }
    }

    fun getMedsForTimeSlot(slot: TimeSlot): List<Medication> {
        val currentMeds = medications.value
        return currentMeds.filter { med ->
            // 1. เงื่อนไขสำหรับ "รายชั่วโมง" (HOURLY)
            // ใช้ hour เมื่อ time และ type เป็น NULL
            if (slot == TimeSlot.HOURLY) {
                return@filter med.time == null && med.type == null && med.hour != null
            }

            // 2. เงื่อนไขสำหรับหมวดปกติ (เช้า, เที่ยง, เย็น, ก่อนนอน)
            // บังคับใช้เฉพาะฟิลด์ 'time' เท่านั้น (ถ้า time เป็น null จะไม่แสดงในหมวดเหล่านี้)
            val timeStr = med.time ?: return@filter false
            
            val hourValue = try {
                timeStr.substringBefore(':').trim().toInt()
            } catch (e: Exception) { -1 }

            if (hourValue == -1) return@filter false

            when (slot) {
                TimeSlot.MORNING -> hourValue in 5..10
                TimeSlot.LUNCH -> hourValue in 11..14
                TimeSlot.EVENING -> hourValue in 15..19
                TimeSlot.BEFORE_BED -> hourValue in 20..23 || hourValue in 0..4
                else -> false
            }
        }
    }

    fun takeMedicine(medication: Medication) {
        if (medication.isTaken) return 
        viewModelScope.launch {
            try {
                val currentTimeStr = dateFormat.format(Date())
                val response = if (medication.logId != null) {
                    RetrofitClient.api.updateMedLog(medication.logId, LogStatusRequest(status = "taken", taken = currentTimeStr))
                } else {
                    RetrofitClient.api.logMedication(medication.id, LogStatusRequest(status = "taken", taken = currentTimeStr))
                }
                if (response.isSuccessful) performFetch(false)
            } catch (e: Exception) {
                Log.e("MedicationVM", "Take Error: ${e.message}")
            }
        }
    }

    fun markAsMissed(medId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.logMedication(medId, LogStatusRequest(status = "missed"))
                performFetch(false)
            } catch (e: Exception) {
                Log.e("MedicationVM", "Missed Error: ${e.message}")
            }
        }
    }

    fun addExtraDose(medId: Int) {
        viewModelScope.launch {
            try {
                val currentTimeStr = dateFormat.format(Date())
                RetrofitClient.api.logMedication(medId, LogStatusRequest(status = "taken", taken = currentTimeStr))
                performFetch(false)
            } catch (e: Exception) {
                Log.e("MedicationVM", "Extra Error: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
