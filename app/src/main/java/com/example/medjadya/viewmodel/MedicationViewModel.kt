package com.example.medjadya.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.data.repository.MedicationRepository
import com.example.medjadya.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class MedicationViewModel(context: Context) : ViewModel() {

    private val repository = MedicationRepository(context)
    
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime

    val medList = mutableStateListOf<Medication>()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        startClock()
        fetchMedications(showLoading = true)
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(30000) 
            }
        }
    }

    fun fetchMedications(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) _isLoading.value = true
            try {
                val response = repository.getAllMeds()
                if (response.isSuccessful) {
                    val meds = response.body() ?: emptyList()
                    
                    val enrichedMeds = supervisorScope {
                        meds.map { med ->
                            async {
                                try {
                                    val medId = med.id ?: return@async med
                                    val schedules = repository.getSchedules(medId)
                                    val logs = repository.getLogsByMedId(medId)
                                    med.copy(schedules = schedules, logs = logs)
                                } catch (e: Exception) {
                                    Log.e("MedicationVM", "Error fetching data for ${med.id}: ${e.message}")
                                    med
                                }
                            }
                        }.awaitAll()
                    }
                    
                    _medications.value = enrichedMeds
                    medList.clear()
                    medList.addAll(enrichedMeds)
                } else {
                    Log.e("MedicationVM", "Fetch failed: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("MedicationVM", "Error fetching medications: ${e.message}")
                }
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    fun getMedsForTimeSlot(slot: TimeSlot, list: List<Medication>): List<Medication> {
        return list.filter { med ->
            val schedules = med.schedules ?: return@filter false
            schedules.any { schedule ->
                val type = schedule.type?.lowercase()
                val isHourly = type == "hourly" || (schedule.time == null && schedule.hour != null)
                
                if (slot == TimeSlot.HOURLY) {
                    return@any isHourly
                } else {
                    if (isHourly) return@any false
                    
                    val timeStr = schedule.time ?: return@any false
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
            }
        }
    }

    fun getMedsForTimeSlot(slot: TimeSlot): List<Medication> = getMedsForTimeSlot(slot, medications.value)

    fun takeMedicine(medId: Int) {
        viewModelScope.launch {
            try {
                val currentTimeStr = dateFormat.format(Date())
                val response = repository.logMedication(medId, "taken", currentTimeStr)
                if (response.isSuccessful) {
                    fetchMedications()
                }
            } catch (e: Exception) {
                Log.e("MedicationVM", "Error taking medicine: ${e.message}")
            }
        }
    }

    fun markAsMissed(medId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.logMedication(medId, "missed", null)
                if (response.isSuccessful) {
                    fetchMedications()
                }
            } catch (e: Exception) {
                Log.e("MedicationVM", "Error marking as missed: ${e.message}")
            }
        }
    }

    fun insertFullMedData(
        name: String,
        medicineType: String,
        amount: String,
        instructions: String,
        startDate: String,
        stopDate: String,
        quantity: Int,
        scheduleItems: List<Triple<String?, String?, String?>>,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("MedicationVM", "Inserting med: $name")
                val medResponse = repository.insertMed(MedRequest(name, medicineType))
                
                if (medResponse.isSuccessful) {
                    val body = medResponse.body()
                    var medId = body?.idmed ?: body?.data?.idmed
                    
                    Log.d("MedicationVM", "Extracted initial Med ID: $medId")

                    // Fallback: If server didn't return ID, find the latest med with this name
                    if (medId == null) {
                        Log.d("MedicationVM", "ID is null, trying fallback to find latest med named $name")
                        val allMedsRes = repository.getAllMeds()
                        if (allMedsRes.isSuccessful) {
                            val latestMed = allMedsRes.body()
                                ?.filter { it.name == name }
                                ?.maxByOrNull { it.id ?: 0 }
                            medId = latestMed?.id
                            Log.d("MedicationVM", "Fallback found Med ID: $medId")
                        }
                    }

                    if (medId != null) {
                        val instRequest = InstructionRequest(
                            amount = amount,
                            instructions = instructions,
                            start_date = startDate,
                            stop_date = stopDate,
                            quantity = quantity,
                            form = medicineType,
                            medId = medId
                        )
                        val instResponse = repository.insertInstruction(medId, instRequest)
                        if (instResponse.isSuccessful) {
                            var hasError = false
                            scheduleItems.forEach { item ->
                                val schedRequest = ScheduleRequest(
                                    time = item.first,
                                    type = item.second,
                                    hour = item.third,
                                    medId = medId
                                )
                                if (!repository.insertSchedule(medId, schedRequest).isSuccessful) {
                                    hasError = true
                                }
                            }
                            fetchMedications()
                            onComplete(true, if (hasError) "บันทึกสำเร็จ แต่อาจมีบางส่วนผิดพลาด" else "บันทึกสำเร็จ")
                        } else {
                            onComplete(false, "ล้มเหลวที่ตาราง Instruction")
                        }
                    } else {
                        onComplete(false, "ไม่ได้รับ ID ยาจากเซิร์ฟเวอร์")
                    }
                } else {
                    onComplete(false, "บันทึกยาหลักล้มเหลว")
                }
            } catch (e: Exception) {
                onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
            }
        }
    }

    fun deleteMed(id: Int) {
        viewModelScope.launch {
            try {
                if (repository.deleteMed(id).isSuccessful) {
                    fetchMedications()
                }
            } catch (e: Exception) {
                Log.e("MedicationVM", "Delete error: ${e.message}")
            }
        }
    }

    fun addExtraDose(medId: Int?) {
        if (medId == null) return
        viewModelScope.launch {
            try {
                val currentTimeStr = dateFormat.format(Date())
                repository.logMedication(medId, "taken", currentTimeStr)
                fetchMedications()
            } catch (e: Exception) {
                Log.e("MedicationVM", "Error adding extra dose: ${e.message}")
            }
        }
    }
}
