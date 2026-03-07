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
                delay(30000) // อัปเดตเวลาทุก 30 วินาที เพื่อให้ UI ตรวจสอบสถานะ Overdue ได้แม่นยำขึ้น
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    fun startRealtimeUpdates() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var firstLoad = true
            while (true) {
                performFetch(showLoading = firstLoad)
                firstLoad = false
                delay(5000) // Polling ทุก 5 วินาที
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
            Log.d("MedicationViewModel", "Fetching medications...")
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
                Log.d("MedicationViewModel", "Successfully updated ${resultList.size} items")
            } else {
                Log.e("MedicationViewModel", "API Error: ${medsResponse.code()} ${medsResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationViewModel", "Network/Parse Error: ${e.message}")
        } finally {
            if (showLoading) _isLoading.value = false
        }
    }

    fun getMedsForTimeSlot(slot: TimeSlot): List<Medication> {
        return medications.value.filter { med ->
            val timeStr = med.time ?: med.hour ?: return@filter false
            val hour = try {
                val cleaned = timeStr.substringBefore(':').trim()
                val parts = cleaned.split(' ')
                parts.last().toInt()
            } catch (e: Exception) { -1 }

            if (hour == -1) return@filter false

            when (slot) {
                TimeSlot.MORNING -> hour in 5..10
                TimeSlot.LUNCH -> hour in 11..14
                TimeSlot.EVENING -> hour in 15..19
                TimeSlot.BEFORE_BED -> hour in 20..23 || hour in 0..4
            }
        }
    }

    fun takeMedicine(medication: Medication) {
        if (medication.isTaken) return 

        val currentList = _medications.value
        val currentTimeStr = dateFormat.format(Date())

        // Optimistic UI Update
        _medications.value = currentList.map {
            if (it.id == medication.id && (it.time == medication.time || it.hour == medication.hour)) {
                it.copy(status = "taken")
            } else it
        }

        viewModelScope.launch {
            try {
                val response = if (medication.logId != null) {
                    RetrofitClient.api.updateMedLog(
                        medication.logId,
                        LogStatusRequest(status = "taken", taken = currentTimeStr)
                    )
                } else {
                    RetrofitClient.api.logMedication(
                        medication.id,
                        LogStatusRequest(status = "taken", taken = currentTimeStr)
                    )
                }
                
                if (response.isSuccessful) {
                    performFetch(showLoading = false)
                } else {
                    _medications.value = currentList
                }
            } catch (e: Exception) {
                _medications.value = currentList
            }
        }
    }

    fun markAsMissed(medId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.logMedication(
                    medId,
                    LogStatusRequest(status = "missed")
                )
                if (response.isSuccessful) {
                    performFetch(showLoading = false)
                }
            } catch (e: Exception) {
                Log.e("API_LOG", "Error logging missed: ${e.message}")
            }
        }
    }

    fun addExtraDose(medId: Int) {
        val currentTimeStr = dateFormat.format(Date())
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.logMedication(
                    medId,
                    LogStatusRequest(status = "taken", taken = currentTimeStr)
                )
                if (response.isSuccessful) {
                    performFetch(showLoading = false)
                }
            } catch (e: Exception) {
                Log.e("API_LOG", "Error adding extra dose: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
