package com.example.medjadya.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medjadya.model.DailySummary
import com.example.medjadya.viewmodel.MedLogViewModel

@Composable
fun RecordScreen(viewModel: MedLogViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.fetchMedLogs(1)
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF1E9EBD), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "บันทึกและประวัติการทานยา",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF0F4F7)
    ) { padding ->
        // ใช้ Column ครอบทั้งหมดไว้
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- ส่วนที่ 1: ปุ่มเลือกช่วงเวลา (อยู่นอก LazyColumn เพื่อให้คงที่) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 16.dp)
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
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(period)
                        }
                    }
                }
            }

            // --- ส่วนที่ 2: รายการที่เลื่อนได้ (LazyColumn) ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 30.dp, end = 30.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ส่วนสรุป (Card สีฟ้า) ยังคงเลื่อนหายได้ตามปกติ
                item {
                    SummaryCardOnly(viewModel)
                }

                item {
                    Text(
                        text = "ประวัติการทานยา",
                        fontWeight = FontWeight.Bold,
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
    var expanded by remember { mutableStateOf(false) } // สถานะ Dropdown

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { expanded = !expanded } // กดเพื่อกางออก/พับเก็บ
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- ส่วนแสดงผลปกติ (Header ของ Card) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = summary.displayDate, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "รับประทานยา ${summary.takenCount} จาก ${summary.totalCount} มื้อ",
                        fontSize = 14.sp, color = Color.Gray
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            // แถบ Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape).background(Color(0xFFE0E0E0))
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxHeight().weight(if (summary.progress > 0) summary.progress else 0.0001f).background(Color(0xFF4CAF50)))
                        if (summary.progress < 1f) {
                            Box(Modifier.fillMaxHeight().weight(1f - summary.progress).background(Color.Red))
                        }
                    }
                }
                Text(
                    text = "${(summary.progress * 100).toInt()}%",
                    modifier = Modifier.padding(start = 12.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
            }

            Text(text = summary.statusText, fontSize = 13.sp, color = Color.Gray)

            // --- ส่วน Dropdown (แสดงเมื่อ expanded == true) ---
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

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
                                Text(text = "  - $medName", fontSize = 13.sp)
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
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E9F0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("สรุปการทานยา", fontWeight = FontWeight.Bold)
            Text(
                "${viewModel.overallProgress.value.toInt()}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5C6BC0)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(icon = Icons.Default.CheckCircle, count = viewModel.totalTaken.value, label = "ทาน", color = Color(0xFF4CAF50))
                StatItem(icon = Icons.Default.Clear, count = viewModel.totalMissed.value, label = "ไม่ทาน", color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun EmptyStateUI() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("ไม่พบประวัติการทานยาในช่วงเวลานี้", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, count: Int, label: String, color: Color) {
    // ใช้ Card เพื่อสร้างกรอบสีขาว
    Card(
        modifier = Modifier
            .width(85.dp) // กำหนดความกว้างให้พอดี
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // พื้นหลังสีขาว
        shape = RoundedCornerShape(12.dp), // ขอบมน
        elevation = CardDefaults.cardElevation(2.dp) // เพิ่มเงาเล็กน้อยให้มีมิติ
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
                modifier = Modifier.size(28.dp)
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
                color = Color.Black
            )
        }
    }
}
