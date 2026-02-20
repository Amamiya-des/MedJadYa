package com.example.medjadya.ui

import android.util.Log
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medjadya.model.Medication
import com.example.medjadya.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Composable
fun MedicationScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val token =
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwiZW1haWwiOiJzdGFtcEBlbWFpbC5jb20iLCJpYXQiOjE3NzE1NzQ5OTMsImV4cCI6MTc3MTY2MTM5M30.gSTC1j4_xYQtYHEHlZXHFGR77W5y7Z8eGOIbO8fNHrs"

            // 1. Get basic med list
            val meds = RetrofitClient.instance.getMeds(token)

            // 2. For each med, fetch its specific instructions and schedules in parallel
            val fullMedications = meds.map { med ->
                val medId = med.id ?: return@map med

                // Fetch extra details for this specific med ID
                val instructions = async { RetrofitClient.instance.getInstructions(token, medId) }
                val schedules = async { RetrofitClient.instance.getSchedules(token, medId) }

                med.copy(
                    instruction = instructions.await().firstOrNull(),
                    schedules = schedules.await()
                )
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
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
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
                    text = if (form == "tablet") "\uD83D\uDD34" else "💊",
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
