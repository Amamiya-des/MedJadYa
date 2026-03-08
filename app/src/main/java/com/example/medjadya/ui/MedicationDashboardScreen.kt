package com.example.medjadya.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medjadya.model.Medication
import com.example.medjadya.model.TimeSlot
import com.example.medjadya.viewmodel.MedicationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDashboardScreen() {
    val context = LocalContext.current
    val viewModel: MedicationViewModel = viewModel { MedicationViewModel(context) }
    
    val medications by viewModel.medications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    var expandedSlot by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMedications(showLoading = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ยาที่ต้องทานวันนี้", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("รายการจากฐานข้อมูล", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchMedications(showLoading = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0097B2))
            )
        }
    ) { padding ->
        if (isLoading && medications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0097B2))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val slots = listOf(
                    Triple("ตอนเช้า", "🌅", TimeSlot.MORNING),
                    Triple("มื้อเที่ยง", "☀️", TimeSlot.LUNCH),
                    Triple("มื้อเย็น", "🌙", TimeSlot.EVENING),
                    Triple("ก่อนนอน", "💤", TimeSlot.BEFORE_BED)
                )

                slots.forEach { (title, icon, slot) ->
                    val medsForSlot = viewModel.getMedsForTimeSlot(slot)
                    item {
                        TimeSlotDropdownCard(
                            title = title,
                            iconLabel = icon,
                            gradient = getGradientForSlot(slot),
                            medications = medsForSlot,
                            currentTime = currentTime,
                            isExpanded = expandedSlot == title,
                            onToggle = { expandedSlot = if (expandedSlot == title) null else title },
                            onTakeClick = { med -> med.id?.let { viewModel.takeMedicine(it) } },
                            onMissed = { medId -> viewModel.markAsMissed(medId) }
                        )
                    }
                }
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
    currentTime: Long,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onTakeClick: (Medication) -> Unit,
    onMissed: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().background(gradient).padding(16.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(iconLabel, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(color = Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(50)) {
                        Text(medications.size.toString(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (medications.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))) {
                        Text("ไม่มีรายการยาในช่วงเวลานี้", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    medications.forEach { med -> 
                        MedicationItemRow(
                            medication = med, 
                            currentTime = currentTime,
                            onTakeClick = onTakeClick,
                            onMissed = onMissed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationItemRow(
    medication: Medication, 
    currentTime: Long,
    onTakeClick: (Medication) -> Unit, 
    onMissed: (Int) -> Unit
) {
    val isTaken = medication.isTaken
    val isMissedInStatus = medication.isMissedStatus
    
    var isProcessing by remember(medication) { mutableStateOf(false) }

    val schedule = medication.schedules?.firstOrNull()
    val isOverdue = remember(medication, currentTime) {
        val timeStr = schedule?.time ?: return@remember false
        try {
            val medHour = timeStr.substringBefore(':').toInt()
            val medMinute = timeStr.substringAfter(':').substringBefore(':').toInt()
            val now = Calendar.getInstance()
            now.timeInMillis = currentTime
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            if (currentHour > medHour) true
            else if (currentHour == medHour && currentMinute > medMinute) true
            else false
        } catch (e: Exception) { false }
    }

    val showAsMissed = !isTaken && (isMissedInStatus || isOverdue)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (showAsMissed) Color(0xFFFFF0F0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${schedule?.time?.take(5) ?: "ไม่ระบุเวลา"} น.",
                        fontSize = 12.sp,
                        color = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                        fontWeight = FontWeight.Bold
                    )
                    if (showAsMissed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• ลืมทาน!", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = medication.name ?: "Unknown", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "${medication.dosage ?: ""} ${medication.form ?: "เม็ด"}", fontSize = 14.sp, color = Color.Gray)
            }
            
            Button(
                onClick = { 
                    isProcessing = true
                    onTakeClick(medication) 
                },
                enabled = !isTaken && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                    disabledContainerColor = if (isTaken) Color(0xFFBDBDBD) else Color.LightGray, 
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when {
                        isTaken -> "ทานแล้ว"
                        isProcessing -> "กำลังบันทึก..."
                        showAsMissed -> "ทานตอนนี้"
                        else -> "ทานยา"
                    },
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun getGradientForSlot(slot: TimeSlot): Brush {
    return when (slot) {
        TimeSlot.MORNING -> Brush.horizontalGradient(listOf(Color(0xFFE6FFEA), Color(0xFFB2FFBD)))
        TimeSlot.LUNCH -> Brush.horizontalGradient(listOf(Color(0xFFFFF5E6), Color(0xFFFFE0B2)))
        TimeSlot.EVENING -> Brush.horizontalGradient(listOf(Color(0xFFE6F7FF), Color(0xFFBAE7FF)))
        TimeSlot.BEFORE_BED -> Brush.horizontalGradient(listOf(Color(0xFFF0E6FF), Color(0xFFD6BCFA)))
    }
}
