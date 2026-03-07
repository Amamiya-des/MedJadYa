package com.example.myapplication.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.medjadya.model.Medication
import com.example.medjadya.network.RetrofitClient
import com.example.medjadya.notification.AlarmReceiver
import kotlinx.coroutines.async
import java.util.*

@Composable
fun MedicationScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Launcher สำหรับขออนุญาตแจ้งเตือน (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "แอปต้องการการอนุญาตเพื่อแจ้งเตือนทานยา", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        // ขออนุญาตแจ้งเตือนเมื่อเปิดหน้านี้
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        try {
            val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwiZW1haWwiOiJzdGFtcEBlbWFpbC5jb20iLCJpYXQiOjE3NzI4NzMyMjQsImV4cCI6MTc3Mjk1OTYyNH0.avS-k7YaS_DIOMQQJL-Zg8aEWum9afUACqUCtYLY29A"
            val meds = RetrofitClient.instance.getMeds(token)
            
            val fullMedications = meds.map { med ->
                val medId = med.id ?: return@map med
                val instructions = async { RetrofitClient.instance.getInstructions(token, medId) }
                val schedules = async { RetrofitClient.instance.getSchedules(token, medId) }
                
                val resultMed = med.copy(
                    instruction = instructions.await().firstOrNull(),
                    schedules = schedules.await()
                )

                // ตั้งปลุกตามเวลาใน Database
                resultMed.schedules?.forEach { schedule ->
                    schedule.time?.let { timeStr ->
                        Log.d("MedicationScreen", "กำลังตั้งเวลาสำหรับ ${resultMed.name} ที่ $timeStr")
                        scheduleAlarm(context, resultMed.name ?: "Unknown", timeStr)
                    }
                }

                resultMed
            }
            
            medications = fullMedications
        } catch (e: Exception) {
            Log.e("MedicationScreen", "Error: ", e)
        } finally {
            isLoading = false
        }
    }

    val filteredMedications = remember(searchQuery, medications) {
        if (searchQuery.isEmpty()) {
            medications
        } else {
            medications.filter {
                it.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF1E9EBD)),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "ยาของฉัน",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text("ค้นหารายการยา", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1E9EBD)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E9EBD))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredMedications) { medication ->
                    MedicationCard(medication)
                }
            }
        }
    }
}

private fun scheduleAlarm(context: Context, medName: String, timeStr: String) {
    val parts = timeStr.split(":")
    if (parts.size < 2) return

    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0) // สำคัญ: ต้องรีเซ็ต millisecond

        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("MED_NAME", medName)
    }

    val requestCode = (medName + timeStr).hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    
    Log.d("MedicationScreen", "ตั้งเวลาสำเร็จ: $medName ที่ ${calendar.time}")
}

@Composable
fun MedicationCard(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFF0F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val form = medication.form?.lowercase() ?: ""
                Text(
                    text = if (form == "tablet") "💊" else "🧪",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = medication.name ?: "Unknown Medication",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    val isRemaining = (medication.remainingCount ?: 0) > 0
                    Surface(
                        color = if (isRemaining) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isRemaining) "ยังเหลืออยู่" else "ยาหมดแล้ว",
                            color = if (isRemaining) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = medication.dosage ?: medication.form ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    medication.schedules?.take(3)?.forEach { schedule ->
                        val time = schedule.time
                        val displayTime = if (time != null && time.contains(":")) {
                            time.substringBeforeLast(":").removePrefix("0") + " น."
                        } else {
                            time ?: "--:--"
                        }

                        Surface(
                            color = Color(0xFFDCEBFF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = displayTime,
                                color = Color(0xFF4A90E2),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
