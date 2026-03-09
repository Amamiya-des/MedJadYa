package com.example.medjadya.ui.screens

import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medjadya.data.model.Medication
import com.example.medjadya.data.model.TimeSlot
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    timeSlot: TimeSlot,
    viewModel: MedicationViewModel = viewModel(),
    onBack: () -> Unit
) {
    val allMedications by viewModel.medications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    
    LaunchedEffect(timeSlot) {
        viewModel.fetchMedications(showLoading = true)
    }

    val medications = remember(allMedications, timeSlot) {
        viewModel.getMedsForTimeSlot(timeSlot)
    }

    val (title, backgroundColor) = when (timeSlot) {
        TimeSlot.MORNING -> "ตอนเช้า" to Color(0xFFE6FFEA)
        TimeSlot.LUNCH -> "มื้อเที่ยง" to Color(0xFFFFF5E6)
        TimeSlot.EVENING -> "มื้อเย็น" to Color(0xFFE6F7FF)
        TimeSlot.BEFORE_BED -> "ก่อนนอน" to Color(0xFFF0E6FF)
        TimeSlot.HOURLY -> "รายชั่วโมง" to Color(0xFFFFFDE7) // สีเหลืองอ่อนสำหรับรายชั่วโมง
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(backgroundColor)
        ) {
            if (isLoading && medications.isEmpty()) {
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
                    items(medications, key = { it.id.toString() + (it.time ?: it.hour ?: "") }) { medication ->
                        MedicationListItem(
                            medication = medication,
                            currentTime = currentTime,
                            onTakeClick = { viewModel.takeMedicine(medication) },
                            onMissed = { medId -> viewModel.markAsMissed(medId) },
                            onAddExtraClick = { viewModel.addExtraDose(medication.id) }
                        )
                    }
                    
                    if (medications.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                                Text("ไม่มีรายการยา", color = Color.Gray)
                            }
                        }
                    }
                }
            }
            
            if (isLoading && medications.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
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
    currentTime: Long,
    onTakeClick: () -> Unit,
    onMissed: (Int) -> Unit,
    onAddExtraClick: () -> Unit
) {
    val isTaken = medication.isTaken
    val isMissedInStatus = medication.isMissedStatus
    
    var isProcessingLocal by remember(medication.status, medication.id) { mutableStateOf(false) }

    val isOverdue = remember(isTaken, medication.status, medication.time, medication.hour, currentTime) {
        if (isTaken) return@remember false
        
        val timeStr = medication.time ?: medication.hour ?: return@remember false
        try {
            val parts = timeStr.substringBefore(':').trim().split(' ').last()
            val medHour = parts.toInt()
            val medMinute = try { timeStr.split(":")[1].take(2).toInt() } catch(e: Exception) { 0 }
            
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

    LaunchedEffect(isOverdue) {
        if (isOverdue && medication.status == null) {
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
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${medication.time?.take(5) ?: medication.hour?.take(5) ?: "--:--"} น.",
                        fontSize = 14.sp, 
                        color = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                        fontWeight = FontWeight.Bold
                    )
                    if (showAsMissed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• ลืมทาน!", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(medication.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${medication.dosage ?: ""} ${medication.form ?: "เม็ด"}", fontSize = 14.sp, color = Color.Gray)
                Text(medication.instruction ?: "ทานตามปกติ", fontSize = 14.sp, color = Color.Gray)
            }
            
            Button(
                onClick = {
                    isProcessingLocal = true
                    onTakeClick()
                },
                enabled = !isTaken && !isProcessingLocal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAsMissed) Color.Red else Color(0xFF0097B2),
                    disabledContainerColor = if (isTaken) Color(0xFFBDBDBD) else Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        isTaken -> "ทานแล้ว"
                        isProcessingLocal -> "กำลังบันทึก..."
                        showAsMissed -> "ทานตอนนี้"
                        else -> "ทานยา"
                    }, 
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            if (medication.name.contains("C", ignoreCase = true)) {
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = onAddExtraClick,
                    modifier = Modifier.size(40.dp),
                    containerColor = Color(0xFF0097B2),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    }
}
