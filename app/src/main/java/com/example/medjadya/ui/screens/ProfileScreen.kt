package com.example.medjadya.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.medjadya.data.local.SessionStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavHostController) {
  val ctx = LocalContext.current
  val store = remember { SessionStore(ctx) }
  val scope = rememberCoroutineScope()

  val name by store.nameFlow.collectAsState(initial = "")
  val email by store.emailFlow.collectAsState(initial = "")

  Column(Modifier.fillMaxSize()) {
    TopAppBar(title = { Text("โปรไฟล์") })

    Card(
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(Modifier.padding(16.dp)) {
        Text(name, style = MaterialTheme.typography.titleLarge)
        Text(email, style = MaterialTheme.typography.bodyMedium)
      }
    }

    Text("เมนูการแจ้งเตือน", modifier = Modifier.padding(start = 16.dp, top = 8.dp), style = MaterialTheme.typography.titleMedium)

    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ElevatedCard(modifier = Modifier.weight(1f), onClick = { nav.navigate("refill") }) {
        Column(Modifier.padding(16.dp)) {
          Text("เตือนให้เติมยา", style = MaterialTheme.typography.titleMedium)
          Text("ตรวจสอบยาที่เหลือน้อย", style = MaterialTheme.typography.bodySmall)
        }
      }
        ElevatedCard(
            modifier = Modifier.weight(1f),
            onClick = { }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("เตือนความจำ", style = MaterialTheme.typography.titleMedium)
                Text("กำลังเข้าใช้งานอยู่", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    Text("การตั้งค่า", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)

    Card(Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
      Column {
        ListItem(headlineContent = { Text("แจ้งเตือน") }, supportingContent = { Text("จัดการการแจ้งเตือน") })
        Divider()
        ListItem(
          headlineContent = { Text("ออกจากระบบ") },
          supportingContent = { Text("ล้างข้อมูลผู้ใช้") },
          modifier = Modifier.clickable {
            scope.launch {
              store.clear()
                nav.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                    launchSingleTop = true
                }
            }
          }
        )
      }
    }
  }
}
