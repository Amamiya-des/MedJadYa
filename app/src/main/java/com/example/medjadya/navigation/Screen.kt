package com.example.medjadya.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Login : Screen("login_screen", "เข้าสู่ระบบ", Icons.Default.Person)
    data object Register : Screen("register_screen", "สมัครสมาชิก", Icons.Default.Person)
    data object EditProfile : Screen("edit_profile_screen", "แก้ไขโปรไฟล์", Icons.Default.Person)
    data object NotificationSettings : Screen("notification_settings_screen", "การแจ้งเตือน", Icons.Default.Notifications)
    data object RefillAlerts : Screen("refill_alerts_screen", "เตือนให้เติมยา", Icons.Default.Warning)

    data object Home : Screen("home", "หน้าหลัก", Icons.Default.Home)
    data object Medicine : Screen("medicine", "ยา", Icons.Default.List)
    data object Record : Screen("record", "บันทึก", Icons.Default.DateRange)
    data object Profile : Screen("profile_screen", "โปรไฟล์", Icons.Default.Person)
    data object Test : Screen("test", "ทดสอบ", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Medicine,
    Screen.Record,
    Screen.Profile
)
