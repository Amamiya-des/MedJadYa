package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertScreen(navController: NavController, viewModel: MedViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ยาเม็ด") }
    
    var scheduleMode by remember { mutableStateOf("กินตามช่วงเวลา") } 
    var mealRelation by remember { mutableStateOf("หลังอาหาร") }
    
    val selectedPeriods = remember { mutableStateMapOf("เช้า" to false, "เที่ยง" to false, "เย็น" to false, "ก่อนนอน" to false) }
    val periodTimes = remember { mutableStateMapOf("เช้า" to "08:00", "เที่ยง" to "12:00", "เย็น" to "18:00", "ก่อนนอน" to "21:00") }
    
    var everyXHours by remember { mutableStateOf("1") }
    
    var startDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var stopDate by remember { mutableStateOf(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE)) }
    var quantity by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStopDatePicker by remember { mutableStateOf(false) }

    val medTypes = listOf(
        Pair("ยาเม็ด", android.R.drawable.ic_menu_gallery),
        Pair("ยาแคปซูล", android.R.drawable.ic_menu_gallery),
        Pair("ยาน้ำ", android.R.drawable.ic_menu_gallery),
        Pair("ยาฉีด", android.R.drawable.ic_menu_gallery)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("เพิ่มข้อมูลยา", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0097A7))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("ยกเลิก", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) { Toast.makeText(context, "กรุณากรอกชื่อยา", Toast.LENGTH_SHORT).show(); return@Button }
                            if (amount.isBlank()) { Toast.makeText(context, "กรุณากรอกปริมาณยา", Toast.LENGTH_SHORT).show(); return@Button }
                            if (quantity.isBlank()) { Toast.makeText(context, "กรุณากรอกจำนวนยา", Toast.LENGTH_SHORT).show(); return@Button }

                            val scheduleItems = mutableListOf<Pair<String?, String>>()
                            if (scheduleMode == "กินตามช่วงเวลา") {
                                selectedPeriods.forEach { (period, isSelected) ->
                                    if (isSelected) {
                                        scheduleItems.add(Pair("${periodTimes[period]}:00", period))
                                    }
                                }
                                if (scheduleItems.isEmpty()) {
                                    Toast.makeText(context, "กรุณาเลือกช่วงเวลาอย่างน้อย 1 ช่วง", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            } else {
                                scheduleItems.add(Pair(null, "ทุกๆ $everyXHours ชั่วโมง"))
                            }

                            Toast.makeText(context, "กำลังบันทึกข้อมูล...", Toast.LENGTH_SHORT).show()
                            viewModel.insertFullMedData(
                                name = name,
                                medicineType = selectedType,
                                amount = amount,
                                instructions = if (scheduleMode == "กินตามช่วงเวลา") mealRelation else "",
                                startDate = startDate,
                                stopDate = stopDate,
                                quantity = quantity.toIntOrNull() ?: 0,
                                scheduleItems = scheduleItems
                            ) { isSuccess, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (isSuccess) {
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7))
                    ) {
                        Text("บันทึก", color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            SectionTitle("ข้อมูลยา")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("ชื่อยา *") }, placeholder = { Text("ระบุชื่อยา") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("ปริมาณยาต่อครั้ง *") }, placeholder = { Text("เช่น 1 เม็ด, 10มล") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("ประเภทของยา *")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedTypeItem(medTypes[0].first, selectedType == medTypes[0].first, Modifier.weight(1f)) { selectedType = it }
                MedTypeItem(medTypes[1].first, selectedType == medTypes[1].first, Modifier.weight(1f)) { selectedType = it }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedTypeItem(medTypes[2].first, selectedType == medTypes[2].first, Modifier.weight(1f)) { selectedType = it }
                MedTypeItem(medTypes[3].first, selectedType == medTypes[3].first, Modifier.weight(1f)) { selectedType = it }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("ช่วงเวลาที่ทานยา")
            
            var modeExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = scheduleMode, onValueChange = {}, readOnly = true,
                    label = { Text("รูปแบบการทาน") },
                    modifier = Modifier.fillMaxWidth().clickable { modeExpanded = true },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    shape = RoundedCornerShape(12.dp), enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray)
                )
                DropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    DropdownMenuItem(text = { Text("กินตามช่วงเวลา") }, onClick = { scheduleMode = "กินตามช่วงเวลา"; modeExpanded = false })
                    DropdownMenuItem(text = { Text("กินทุกๆกี่ชั่วโมง") }, onClick = { scheduleMode = "กินทุกๆกี่ชั่วโมง"; modeExpanded = false })
                }
            }

            if (scheduleMode == "กินตามช่วงเวลา") {
                Spacer(modifier = Modifier.height(16.dp))
                Text("ความสัมพันธ์กับมื้ออาหาร:", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = mealRelation == "ก่อนอาหาร", onClick = { mealRelation = "ก่อนอาหาร" })
                    Text("ก่อนอาหาร")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = mealRelation == "หลังอาหาร", onClick = { mealRelation = "หลังอาหาร" })
                    Text("หลังอาหาร")
                }

                PeriodSelectionRow("เช้า", "05:00-11:59", 5, 11, selectedPeriods["เช้า"]!!, periodTimes["เช้า"]!!) { isSel, time ->
                    selectedPeriods["เช้า"] = isSel
                    periodTimes["เช้า"] = time
                }
                PeriodSelectionRow("เที่ยง", "12:00-15:59", 12, 15, selectedPeriods["เที่ยง"]!!, periodTimes["เที่ยง"]!!) { isSel, time ->
                    selectedPeriods["เที่ยง"] = isSel
                    periodTimes["เที่ยง"] = time
                }
                PeriodSelectionRow("เย็น", "16:00-19:59", 16, 19, selectedPeriods["เย็น"]!!, periodTimes["เย็น"]!!) { isSel, time ->
                    selectedPeriods["เย็น"] = isSel
                    periodTimes["เย็น"] = time
                }
                PeriodSelectionRow("ก่อนนอน", "20:00-23:59", 20, 23, selectedPeriods["ก่อนนอน"]!!, periodTimes["ก่อนนอน"]!!) { isSel, time ->
                    selectedPeriods["ก่อนนอน"] = isSel
                    periodTimes["ก่อนนอน"] = time
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text("ทานยาทุกๆ (ชั่วโมง):", fontWeight = FontWeight.Medium)
                var hourExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { hourExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("$everyXHours ชั่วโมง")
                    }
                    DropdownMenu(expanded = hourExpanded, onDismissRequest = { hourExpanded = false }) {
                        (1..24).forEach { hr ->
                            DropdownMenuItem(text = { Text("$hr ชั่วโมง") }, onClick = { everyXHours = hr.toString(); hourExpanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("สต็อกและระยะเวลา")
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("จำนวนยาทั้งหมดที่มี *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startDate, onValueChange = {}, label = { Text("เริ่มกิน") }, modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }, shape = RoundedCornerShape(12.dp), enabled = false, trailingIcon = { Icon(Icons.Default.DateRange, null) }, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black))
                OutlinedTextField(value = stopDate, onValueChange = {}, label = { Text("สิ้นสุด") }, modifier = Modifier.weight(1f).clickable { showStopDatePicker = true }, shape = RoundedCornerShape(12.dp), enabled = false, trailingIcon = { Icon(Icons.Default.DateRange, null) }, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black))
            }
        }
    }

    if (showStartDatePicker) MyDatePickerDialog(onDateSelected = { startDate = it }, onDismiss = { showStartDatePicker = false })
    if (showStopDatePicker) MyDatePickerDialog(onDateSelected = { stopDate = it }, onDismiss = { showStopDatePicker = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelectionRow(period: String, range: String, minHour: Int, maxHour: Int, isSelected: Boolean, time: String, onUpdate: (Boolean, String) -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = isSelected, onCheckedChange = { onUpdate(it, time) })
            Text(period, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
            Text("($range)", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            if (isSelected) {
                OutlinedButton(onClick = { showTimePicker = true }, shape = RoundedCornerShape(8.dp)) {
                    Text(time)
                }
            }
        }
        if (showTimePicker) {
            val timeParts = time.split(":")
            val state = rememberTimePickerState(initialHour = timeParts[0].toInt(), initialMinute = timeParts[1].toInt())
            AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = {
                TextButton(onClick = {
                    val formatted = String.format(Locale.getDefault(), "%02d:%02d", state.hour, state.minute)
                    onUpdate(isSelected, formatted)
                    showTimePicker = false
                }) { Text("ตกลง") }
            }, text = { TimePicker(state = state) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(onDateSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            state.selectedDateMillis?.let {
                val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                onDateSelected(date.format(DateTimeFormatter.ISO_DATE))
            }
            onDismiss()
        }) { Text("ตกลง") }
    }) { DatePicker(state = state) }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color(0xFF0097A7),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun MedTypeItem(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick(title) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0097A7) else Color.White
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
