package com.example.medjadya.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.medjadya.R
import com.example.medjadya.navigation.Screen
import com.example.medjadya.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: AuthViewModel) {
    val context = LocalContext.current

    val profile = viewModel.userProfile
    val errorMsg = viewModel.errorMessage
    val userId = viewModel.currentUserId

    var showLogoutDialog by remember { mutableStateOf(false) }
    var rememberId by remember { mutableStateOf(false) }

    // Fetch profile when screen opens or when userId changes
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchProfile(userId)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "ยืนยันการออกจากระบบ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "คุณต้องการออกจากระบบใช่หรือไม่?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = rememberId,
                                onClick = { rememberId = !rememberId },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberId,
                            onCheckedChange = { rememberId = it }
                        )
                        Text(
                            text = "จดจำชื่อผู้ใช้ของฉัน",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(rememberUser = rememberId)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097B2))
                ) {
                    Text("ออกจากระบบ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("ยกเลิก", color = Color(0xFF0097B2))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1F5FE))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0097B2))
                .padding(vertical = 20.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "โปรไฟล์",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (profile != null) {
                            Text(
                                text = profile.name ?: "ไม่ทราบชื่อ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.email ?: "ไม่ทราบอีเมล",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "อายุ: ${profile.age ?: "-"} ปี",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        } else {
                            if (errorMsg.isNotEmpty()) {
                                Text(
                                    text = errorMsg,
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                                TextButton(onClick = { viewModel.fetchProfile(userId) }) {
                                    Text("ลองใหม่", color = Color(0xFF0097B2))
                                }
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0097B2)
                                )
                                Text(
                                    text = "กำลังโหลดข้อมูล...",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Notification Section
            Text(
                text = "เมนูการแจ้งเตือน",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "เตือนให้เติมยา",
                    subtitle = "ตรวจสอบยา\nที่เหลือน้อย",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFD32F2F),
                    backgroundColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Screen.RefillAlerts.route)
                    }
                )
                MenuCard(
                    title = "เตือนความจำ",
                    subtitle = "ดูการแจ้งเตือน\nที่เปิดใช้งาน",
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFF0097B2),
                    backgroundColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        Toast.makeText(context, "กำลังพัฒนาระบบเตือนความจำ", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Section
            Text(
                text = "การตั้งค่า",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsItem(
                        title = "การแจ้งเตือน",
                        subtitle = "เปิด/ปิด การแจ้งเตือนแอป",
                        icon = Icons.Default.Notifications,
                        iconTintColor = Color(0xFF0097B2),
                        iconBgColor = Color(0xFFE1F5FE),
                        onClick = {
                            navController.navigate(Screen.NotificationSettings.route)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    SettingsItem(
                        title = "แก้ไขโปรไฟล์",
                        subtitle = "แก้ไขข้อมูลส่วนตัวของคุณ",
                        icon = Icons.Default.Edit,
                        iconTintColor = Color(0xFF0097B2),
                        iconBgColor = Color(0xFFE1F5FE),
                        onClick = {
                            navController.navigate(Screen.EditProfile.route)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                    SettingsItem(
                        title = "ออกจากระบบ",
                        subtitle = null,
                        icon = Icons.Default.ExitToApp,
                        iconTintColor = Color(0xFFD32F2F),
                        iconBgColor = Color(0xFFFFEBEE),
                        onClick = {
                            showLogoutDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    iconTintColor: Color,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTintColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
