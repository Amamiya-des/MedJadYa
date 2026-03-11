package com.example.medjadya.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.data.repository.MedicationRepository
import com.example.medjadya.model.*
import com.example.medjadya.notification.AlarmReceiver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MedicationViewModel(private val context: Context) : ViewModel() {

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
                                    val instructions = repository.getInstructions(medId)

                                    med.copy(
                                        schedules = schedules,
                                        logs = logs,
                                        instruction = instructions.firstOrNull()
                                    )
                                } catch (e: Exception) {
                                    Log.e(
                                        "MedicationVM",
                                        "Error fetching data for ${med.id}: ${e.message}"
                                    )
                                    med
                                }
                            }
                        }.awaitAll()
                    }

                    // --- Date filtering and auto-deletion logic ---
                    val today = LocalDate.now()
                    val activeMeds = mutableListOf<Medication>()

                    enrichedMeds.forEach { med ->
                        val instruction = med.instruction
                        if (instruction != null) {
                            val startDateStr = instruction.start_date
                            val stopDateStr = instruction.stop_date

                            try {
                                // Extract date part if it contains time (e.g. 2026-03-04T17:00:00.000Z -> 2026-03-04)
                                val cleanStart = startDateStr?.split("T")?.get(0)
                                val cleanStop = stopDateStr?.split("T")?.get(0)

                                val startDate =
                                    if (cleanStart != null) LocalDate.parse(cleanStart) else null
                                val stopDate =
                                    if (cleanStop != null) LocalDate.parse(cleanStop) else null
                                Log.d(
                                    "MedicationVM",
                                    "Checking ${med.name}: Today=$today, Start=$startDate, Stop=$stopDate"
                                )
                                when {
                                    // 1. Delete if today has reached or passed stop date
                                    stopDate != null && (today.isAfter(stopDate) || today.isEqual(
                                        stopDate
                                    )) -> {
                                        Log.d(
                                            "MedicationVM",
                                            "Auto-deleting expired med: ${med.name} (Stop date: $stopDate reached)"
                                        )
                                        med.id?.let { deleteMedSilently(it) }
                                    }
                                    // 2. Only show if today is on or after start date
                                    startDate != null && today.isBefore(startDate) -> {
                                        Log.d(
                                            "MedicationVM",
                                            "Hiding med: ${med.name} (Start date: $startDate not yet reached)"
                                        )
                                    }
                                    // 3. Otherwise, it's active
                                    else -> {

                                        Log.d("MedicationVM", "Showing: ${med.name} (Valid)")
                                        activeMeds.add(med)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "MedicationVM",
                                    "Error parsing dates for ${med.name}: ${e.message}"
                                )
                                activeMeds.add(med) // Fallback: show it if parsing fails
                            }
                        } else {
                            activeMeds.add(med) // Med has no instructions, just show it
                        }
                    }

                    _medications.value = activeMeds
                    medList.clear()
                    medList.addAll(activeMeds)
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

    private suspend fun deleteMedSilently(id: Int) {
        try {
            repository.deleteMed(id)
        } catch (e: Exception) {
            Log.e("MedicationVM", "Failed to auto-delete med $id: ${e.message}")
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

                    val timeStr = schedule.time ?: schedule.hour ?: return@any false
                    val hour = try {
                        timeStr.substringBefore(':').trim().toInt()
                    } catch (e: Exception) {
                        -1
                    }

                    when (slot) {
                        TimeSlot.MORNING -> hour in 5..11
                        TimeSlot.LUNCH -> hour in 12..15
                        TimeSlot.EVENING -> hour in 16..19
                        TimeSlot.BEFORE_BED -> hour in 20..23 || hour in 0..4
                        else -> false
                    }
                }
            }
        }
    }

    fun getMedsForTimeSlot(slot: TimeSlot): List<Medication> =
        getMedsForTimeSlot(slot, medications.value)

    fun takeMedicine(medication: Medication) {
        val medId = medication.id ?: return
        viewModelScope.launch {
            try {
                // 1. Log dose
                val currentTimeStr = dateFormat.format(Date())
                val logRes = repository.logMedication(medId, "taken", currentTimeStr)

                if (logRes.isSuccessful) {
                    // 2. Fetch fresh stock data
                    val latest =
                        repository.getInstructions(medId).firstOrNull() ?: medication.instruction

                    if (latest != null) {
                        val current = latest.remain ?: 0
                        val total = latest.quantity ?: 1
                        val dosageStr = latest.amount ?: "0"
                        val dosage = dosageStr.filter { it.isDigit() }.toIntOrNull() ?: 1

                        val nextRemain = (current - dosage).coerceAtLeast(0)

                        // 3. Update server
                        val update = UpdateInstructionRequest(
                            amount = latest.amount,
                            instructions = latest.instructions,
                            quantity = latest.quantity,
                            remain = nextRemain,
                            start_date = latest.start_date,
                            stop_date = latest.stop_date
                        )

                        latest.idinstruction?.let { id ->
                            repository.updateInstruction(id, update)

                            // 4. Trigger Refill Notification if status is LOW or CRITICAL
                            val status = getStockStatus(nextRemain, total)
                            if (status != StockStatus.OK) {
                                AlarmReceiver.sendRefillNotification(
                                    context,
                                    medication.name ?: "ยา"
                                )
                            }
                        }
                    }
                }
                fetchMedications()
            } catch (e: Exception) {
                Log.e("MedicationVM", "Error in takeMedicine: ${e.message}")
            }
        }
    }

    fun markAsMissed(medId: Int) {
        viewModelScope.launch {
            try {
                if (repository.logMedication(medId, "missed", null).isSuccessful) {
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
                val medResponse = repository.insertMed(MedRequest(name, medicineType))
                if (medResponse.isSuccessful) {
                    val body = medResponse.body()
                    var medId = body?.idmed ?: body?.data?.idmed

                    if (medId == null) {
                        val all = repository.getAllMeds()
                        if (all.isSuccessful) {
                            medId = all.body()?.filter { it.name == name }
                                ?.maxByOrNull { it.id ?: 0 }?.id
                        }
                    }

                    if (medId != null) {
                        val inst = InstructionRequest(
                            amount = amount,
                            instructions = instructions,
                            start_date = startDate,
                            stop_date = stopDate,
                            quantity = quantity,
                            remain = quantity,
                            form = medicineType,
                            medId = medId
                        )
                        if (repository.insertInstruction(medId, inst).isSuccessful) {
                            scheduleItems.forEach { item ->
                                val sched = ScheduleRequest(
                                    time = item.first,
                                    type = item.second,
                                    hour = item.third,
                                    medId = medId
                                )
                                repository.insertSchedule(medId, sched)
                            }
                            fetchMedications()
                            onComplete(true, "บันทึกสำเร็จ")
                        } else onComplete(false, "ล้มเหลวที่ตาราง Instruction")
                    } else onComplete(false, "ไม่ได้รับ ID ยา")
                } else onComplete(false, "บันทึกยาหลักล้มเหลว")
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
                Log.e("MedicationVM", "Error: ${e.message}")
            }
        }
    }
}
