package com.example.medjadya.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medjadya.model.DailySummary
import com.example.medjadya.viewmodel.MedLogViewModel
import com.example.medjadya.viewmodel.MedLogViewModelFactory

@Composable
fun RecordScreen(userId: Int) {
    val context = LocalContext.current
    val viewModel: MedLogViewModel = viewModel(factory = MedLogViewModelFactory(context))

    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.fetchMedLogs(userId)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E9EBD), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "บันทึกและประวัติการทานยา",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF0F4F7)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- ส่วนที่ 1: ปุ่มเลือกช่วงเวลา ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("ช่วงเวลา", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ทั้งหมด", "เดือน", "สัปดาห์").forEach { period ->
                        val isSelected = viewModel.selectedPeriod.value == period
                        Button(
                            onClick = {
                                viewModel.selectedPeriod.value = period
                                viewModel.filterData(period, viewModel.allLogsList)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF2E9ABF) else Color.White,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(period)
                        }
                    }
                }
            }

            // --- ส่วนที่ 2: รายการที่เลื่อนได้ ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryCardOnly(viewModel)
                }

                item {
                    Text(
                        text = "ประวัติการทานยา",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (viewModel.dailySummaries.isEmpty()) {
                    item {
                        EmptyStateUI()
                    }
                } else {
                    items(viewModel.dailySummaries) { summary ->
                        MedHistoryCard(summary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedHistoryCard(summary: DailySummary) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    summary.displayDate?.let { Text(text = it, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    Text(
                        text = "รับประทานยา ${summary.takenCount} จาก ${summary.totalCount} มื้อ",
                        fontSize = 14.sp, color = Color.Gray
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                ) {
                    val progress = summary.progress.coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(if (progress >= 1f) Color(0xFF4CAF50) else Color(0xFFFFA726))
                    )
                }
                Text(
                    text = "${(summary.progress * 100).toInt()}%",
                    modifier = Modifier.padding(start = 12.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E9ABF)
                )
            }

            Text(
                text = summary.statusText,
                fontSize = 13.sp,
                color = if (summary.takenCount == summary.totalCount) Color(0xFF4CAF50) else Color.Gray
            )

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))

                if (summary.meals.isEmpty()) {
                    Text(text = "ไม่มีข้อมูลมื้อยา", fontSize = 13.sp, color = Color.LightGray)
                } else {
                    summary.meals.forEach { meal ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "มื้อ${meal.mealName}:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2E9ABF)
                            )
                            if (meal.meds.isEmpty()) {
                                Text(text = "  - ไม่มียาในมื้อนี้", fontSize = 13.sp, color = Color.LightGray)
                            } else {
                                meal.meds.forEach { medName ->
                                    Text(text = "  - ${medName ?: "ไม่ระบุชื่อยา"}", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCardOnly(viewModel: MedLogViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text("สรุปการทานยา", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "${viewModel.overallProgress.value.toInt()}%",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E9EBD)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(icon = Icons.Default.CheckCircle, count = viewModel.totalTaken.value, label = "ทานแล้ว", color = Color(0xFF4CAF50))
                StatItem(icon = Icons.Default.Cancel, count = viewModel.totalMissed.value, label = "ยังไม่ทาน", color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun EmptyStateUI() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("ไม่พบประวัติการทานยา", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, count: Int, label: String, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
