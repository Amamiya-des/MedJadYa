package com.example.medjadya.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.medjadya.data.api.ApiClient
import com.example.medjadya.data.local.SessionStore
import com.example.medjadya.data.model.MedDto
import com.example.medjadya.data.model.RefillItemUi
import com.example.medjadya.data.model.StockStatus
import com.example.medjadya.data.model.stockStatus
import kotlinx.coroutines.launch

@Composable
fun RefillAlertsScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val store = remember { SessionStore(ctx) }
    val uid by store.userIdFlow.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    var meds by remember { mutableStateOf<List<MedDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val refreshData: suspend () -> Unit = {
        if (uid != 0) {
            loading = true
            try {
                meds = ApiClient.api.getMeds(uid)
            } catch (_: Exception) {
                // handle error silently or with a Toast
            }
            loading = false
        } else {
            loading = false
        }
    }

    LaunchedEffect(uid) {
        refreshData()
    }

    // Correctly map from MedDto to RefillItemUi
    // According to JSON: remain and total (quantity) are inside the instruction object.
    val itemsFromDb = meds.map { med ->
        RefillItemUi(
            id = med.idMed,
            name = med.medName ?: "ไม่ระบุชื่อยา",
            doseText = med.instruction?.instructions ?: "",
            remain = med.instruction?.remain ?: 0,
            total = med.instruction?.quantity ?: 0
        )
    }

    var uiList by remember(itemsFromDb) { mutableStateOf(itemsFromDb) }

    LaunchedEffect(itemsFromDb) {
        uiList = itemsFromDb
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        RefillAlertScreenContent(
            items = uiList,
            onBack = {
                nav.popBackStack()
            },
            onRefillClicked = { item ->
                scope.launch {
                    try {
                        ApiClient.api.refill(item.id)
                        Toast.makeText(ctx, "เติมยาแล้ว", Toast.LENGTH_SHORT).show()
                        uiList = uiList.filterNot { it.id == item.id }
                    } catch (_: Exception) {
                        Toast.makeText(ctx, "เติมยาไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefillAlertScreenContent(
    items: List<RefillItemUi>,
    onBack: () -> Unit,
    onRefillClicked: (RefillItemUi) -> Unit
) {
    val needRefillCount = items.count { stockStatus(it.remain, it.total) != StockStatus.OK }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("แจ้งเตือนการเติมยา", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF12A8D6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2FBFF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SummaryCard(needRefillCount = needRefillCount)
            }

            // Only show items that actually need refill (LOW or CRITICAL)
            val refillNeededList = items.filter { stockStatus(it.remain, it.total) != StockStatus.OK }
            
            if (refillNeededList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("ไม่มียาที่ต้องเติมในขณะนี้", color = Color.Gray)
                    }
                }
            } else {
                items(refillNeededList) { med ->
                    RefillAlertCard(
                        item = med,
                        onRefillClicked = { onRefillClicked(med) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(needRefillCount: Int) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2DE))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFD7A8)),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color(0xFFE53935), fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ยาจำนวน $needRefillCount รายการ\nต้องได้รับการเติม",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "อย่าปล่อยให้ยาของคุณหมด\nสั่งเติมยาล่วงหน้าเพื่อให้การรักษาเป็นไปตามแผน\nอย่างต่อเนื่อง",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6B6B)
                )
            }
        }
    }
}

@Composable
private fun RefillAlertCard(
    item: RefillItemUi,
    onRefillClicked: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val status = stockStatus(item.remain, item.total)

    val borderColor = when (status) {
        StockStatus.CRITICAL -> Color(0xFFE53935)
        StockStatus.LOW -> Color(0xFFFF9800)
        StockStatus.OK -> Color(0xFFFFB74D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F3F3)),
                    contentAlignment = Alignment.Center
                ) { Text("💊") }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        if (status != StockStatus.OK) StatusPill(status)
                    }
                    Text(
                        item.doseText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF555555)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "ปริมาณยาคงเหลือ: ${item.remain} / ${item.total} เม็ด",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            val progress = if (item.total > 0)
                (item.remain.toFloat() / item.total.toFloat()).coerceIn(0f, 1f)
            else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "เหลือใช้ได้อีกประมาณ ${estimateDays(item.remain)} วัน",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B6B6B)
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onRefillClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D67F2))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("ทำเครื่องหมายว่าเติมยาแล้ว", color = Color.White)
            }
        }
    }
}

@Composable
private fun StatusPill(status: StockStatus) {
    val (bg, text) = when (status) {
        StockStatus.CRITICAL -> Color(0xFFE53935) to "ใกล้หมด"
        StockStatus.LOW -> Color(0xFFFF9800) to "เหลือน้อย"
        StockStatus.OK -> Color.Transparent to ""
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

private fun estimateDays(remain: Int): Int = when {
    remain <= 0 -> 0
    remain <= 5 -> 3
    remain <= 12 -> 8
    else -> 14
}
