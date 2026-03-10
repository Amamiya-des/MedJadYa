package com.example.medjadya.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medjadya.model.Medication
import com.example.medjadya.model.TimeSlot
import com.example.medjadya.viewmodel.MedicationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDashboardScreen(
    viewModel: MedicationViewModel,
    navController: NavController
) {
    val medications by viewModel.medications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    var expandedSlot by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMedications(showLoading = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7F9)) // สีพื้นหลังเทาอมฟ้าอ่อนๆ เหมือนรูปที่ 3
    ) {
        // --- ส่วนที่ 1: Custom Header สีฟ้า (แทน TopAppBar) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp) // ความสูงเท่ากับรูปที่ 3
                .background(Color(0xFF0097B2)),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ยาที่ต้องทานวันนี้",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // ปุ่ม Refresh ย้ายมาไว้ใน Box Header
                IconButton(
                    onClick = { viewModel.fetchMedications(true) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F7F9))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                val slots = listOf(
                    Triple("ตอนเช้า", "🌅", TimeSlot.MORNING),
                    Triple("มื้อเที่ยง", "☀️", TimeSlot.LUNCH),
                    Triple("มื้อเย็น", "🌙", TimeSlot.EVENING),
                    Triple("ก่อนนอน", "💤", TimeSlot.BEFORE_BED),
                    Triple("รายชั่วโมง", "⏰", TimeSlot.HOURLY)
                )

                slots.forEach { (title, icon, slot) ->
                    // Pass the medications state list to ensure reactivity
                    val medsForSlot = viewModel.getMedsForTimeSlot(slot, medications)
                    item(key = title) {
                        TimeSlotDropdownCard(
                            title = title,
                            iconLabel = icon,
                            gradient = getGradientForSlot(slot),
                            medications = medsForSlot,
                            slot = slot,
                            currentTime = currentTime,
                            isExpanded = expandedSlot == title,
                            onToggle = {
                                expandedSlot = if (expandedSlot == title) null else title
                            },
                            onTakeClick = { med -> med.id?.let { viewModel.takeMedicine(it) } },
                            onDetailClick = { navController.navigate("medication_list/${slot.name}") }
                        )
                    }
                }
            }

            if (isLoading && medications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0097B2))
                }
            } else if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0097B2),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun TimeSlotDropdownCard(
    title: String,
    iconLabel: String,
    gradient: Brush,
    medications: List<Medication>,
    slot: TimeSlot,
    currentTime: Long,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onTakeClick: (Medication) -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(iconLabel, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { onDetailClick() }
                    ) {
                        Text(
                            "${medications.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (medications.isEmpty()) {
                        Text(
                            "ไม่มีรายการยา",
                            modifier = Modifier.padding(8.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        medications.forEach { med ->
                            MedicationItemRow(
                                medication = med,
                                slot = slot,
                                currentTime = currentTime,
                                onTakeClick = onTakeClick
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationItemRow(
    medication: Medication,
    slot: TimeSlot,
    currentTime: Long,
    onTakeClick: (Medication) -> Unit
) {
    val isTakenInSlot = medication.isTakenInSlot(slot)

    // Find the schedule time that matches the current time slot
    val medTimeStr = remember(medication.schedules, slot) {
        medication.schedules?.find { schedule ->
            val type = schedule.type?.lowercase()
            val isHourly = type == "hourly" || (schedule.time == null && schedule.hour != null)

            if (slot == TimeSlot.HOURLY) {
                return@find isHourly
            }

            if (isHourly) return@find false

            val timeStr = schedule.time ?: schedule.hour ?: return@find false
            val hour = try {
                timeStr.substringBefore(':').trim().toInt()
            } catch (e: Exception) {
                -1
            }

            when (slot) {
                TimeSlot.MORNING -> hour in 5..10
                TimeSlot.LUNCH -> hour in 11..14
                TimeSlot.EVENING -> hour in 15..19
                TimeSlot.BEFORE_BED -> hour in 20..23 || hour in 0..4
                else -> false
            }
        }?.let { schedule ->
            if (slot == TimeSlot.HOURLY) {
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
        if (slot == TimeSlot.HOURLY) return@remember false // Hourly meds are never "overdue" in this simple logic
        try {
            val parts = medTimeStr.substringBefore(':').trim().split(' ').last()
            val medHour = parts.toInt()
            val now = Calendar.getInstance().apply { timeInMillis = currentTime }
            now.get(Calendar.HOUR_OF_DAY) > medHour
        } catch (e: Exception) {
            false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isOverdue) Color(0xFFFFF0F0) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (slot == TimeSlot.HOURLY) medTimeStr else "${medTimeStr.take(5)} น." + if (isOverdue) " • ลืมทาน!" else "",
                fontSize = 12.sp,
                color = if (isOverdue) Color.Red else Color(0xFF0097B2),
                fontWeight = FontWeight.Bold
            )
            Text(medication.name ?: "ไม่ระบุชื่อยา", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { onTakeClick(medication) },
            enabled = !isTakenInSlot,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOverdue) Color.Red else Color(
                    0xFF0097B2
                )
            )
        ) {
            Text(if (isTakenInSlot) "ทานแล้ว" else "ทานยา", fontSize = 11.sp)
        }
    }
}

fun getGradientForSlot(slot: TimeSlot): Brush {
    return when (slot) {
        TimeSlot.MORNING -> Brush.horizontalGradient(listOf(Color(0xFFE6FFEA), Color(0xFFB2FFBD)))
        TimeSlot.LUNCH -> Brush.horizontalGradient(listOf(Color(0xFFFFF5E6), Color(0xFFFFE0B2)))
        TimeSlot.EVENING -> Brush.horizontalGradient(listOf(Color(0xFFE6F7FF), Color(0xFFBAE7FF)))
        TimeSlot.BEFORE_BED -> Brush.horizontalGradient(
            listOf(
                Color(0xFFF0E6FF),
                Color(0xFFD6BCFA)
            )
        )

        TimeSlot.HOURLY -> Brush.horizontalGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D)))
    }
}
