package com.example.medjadya.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medjadya.model.Medication
import com.example.medjadya.model.TimeSlot
import com.example.medjadya.viewmodel.MedicationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    timeSlot: TimeSlot,
    viewModel: MedicationViewModel,
    targetMedName: String? = null,
    onBack: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    LaunchedEffect(timeSlot) {
        viewModel.fetchMedications(showLoading = true)
    }

    val filteredMedications = remember(medications, timeSlot, targetMedName) {
        val baseList = viewModel.getMedsForTimeSlot(timeSlot, medications)
        if (!targetMedName.isNullOrEmpty()) {
            baseList.filter { it.name?.equals(targetMedName, ignoreCase = true) == true }
        } else {
            baseList
        }
    }

    val (title, backgroundColor) = when (timeSlot) {
        TimeSlot.MORNING -> "ตอนเช้า" to Color(0xFFE6FFEA)
        TimeSlot.LUNCH -> "มื้อเที่ยง" to Color(0xFFFFF5E6)
        TimeSlot.EVENING -> "มื้อเย็น" to Color(0xFFE6F7FF)
        TimeSlot.BEFORE_BED -> "ก่อนนอน" to Color(0xFFF0E6FF)
        TimeSlot.HOURLY -> "รายชั่วโมง" to Color(0xFFFFFDE7)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(Color(0xFF0097B2)),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (targetMedName != null) "แจ้งเตือน: $targetMedName" else title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { viewModel.fetchMedications(showLoading = true) }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            if (isLoading && filteredMedications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0097B2))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        filteredMedications,
                        key = { med -> med.id.toString() + timeSlot.name }) { medication ->
                        MedicationListItem(
                            medication = medication,
                            timeSlot = timeSlot,
                            currentTime = currentTime,
                            onTakeClick = { viewModel.takeMedicine(medication) },
                            onMissed = { medId -> viewModel.markAsMissed(medId) },
                            onAddExtraClick = { viewModel.addExtraDose(medication.id) }
                        )
                    }

                    if (filteredMedications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (targetMedName != null) "ไม่พบข้อมูลยา: $targetMedName" else "ไม่มีรายการยา",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading && filteredMedications.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFF0097B2),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    timeSlot: TimeSlot,
    currentTime: Long,
    onTakeClick: () -> Unit,
    onMissed: (Int) -> Unit,
    onAddExtraClick: () -> Unit
) {
    val isTakenInSlot = medication.isTakenInSlot(timeSlot)

    val medTimeStr = remember(medication.schedules, timeSlot) {
        medication.schedules?.find { schedule ->
            val type = schedule.type?.lowercase()
            val isHourly = type == "hourly" || (schedule.time == null && schedule.hour != null)

            if (timeSlot == TimeSlot.HOURLY) {
                return@find isHourly
            }

            if (isHourly) return@find false

            val timeStr = schedule.time ?: schedule.hour ?: return@find false
            val hour = try {
                timeStr.substringBefore(':').trim().toInt()
            } catch (e: Exception) {
                -1
            }

            when (timeSlot) {
                TimeSlot.MORNING -> hour in 5..11
                TimeSlot.LUNCH -> hour in 12..15
                TimeSlot.EVENING -> hour in 16..19
                TimeSlot.BEFORE_BED -> hour in 20..23 || hour in 0..4
                else -> false
            }
        }?.let { schedule ->
            if (timeSlot == TimeSlot.HOURLY) {
                val hourStr = schedule.hour ?: schedule.time ?: ""
                val hourVal = try {
                    hourStr.substringBefore(':').trim().toInt().toString()
                } catch (e: Exception) {
                    "?"
                }
                "ทุกๆ $hourVal ชม."
            } else {
                schedule.time ?: schedule.hour
            }
        } ?: "--:--"
    }


    val isOverdue = remember(isTakenInSlot, medTimeStr, currentTime) {
        if (isTakenInSlot) return@remember false
        if (timeSlot == TimeSlot.HOURLY) return@remember false

        try {
            val parts = medTimeStr.substringBefore(':').trim().split(' ').last()
            val medHour = parts.toInt()
            val medMinute = try {
                medTimeStr.split(":")[1].take(2).toInt()
            } catch (e: Exception) {
                0
            }

            val now = Calendar.getInstance()
            now.timeInMillis = currentTime
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)

            if (currentHour > medHour) true
            else if (currentHour == medHour && currentMinute > medMinute) true
            else false
        } catch (e: Exception) {
            false
        }
    }

    val showAsMissed = !isTakenInSlot && isOverdue

    LaunchedEffect(isOverdue) {
        if (isOverdue && !isTakenInSlot && medication.id != null) {
            onMissed(medication.id)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (showAsMissed) Color(0xFFFFF0F0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (timeSlot == TimeSlot.HOURLY) medTimeStr else "${medTimeStr.take(5)} น.",
                        fontSize = 14.sp,
                        color = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                        fontWeight = FontWeight.Bold
                    )
                    if (showAsMissed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ลืมทาน!",
                            fontSize = 12.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    medication.name ?: "ไม่ระบุชื่อยา",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${medication.instruction?.amount ?: ""} ${medication.form ?: "เม็ด"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    medication.instruction?.instructions ?: "ทานตามปกติ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onTakeClick,
                enabled = !isTakenInSlot,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                    disabledContainerColor = if (isTakenInSlot) Color(0xFFBDBDBD) else Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isTakenInSlot) "ทานแล้ว" else "ทานยา",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            if (medication.name?.contains("C", ignoreCase = true) == true) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddExtraClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF0097B2), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        }
    }
}
