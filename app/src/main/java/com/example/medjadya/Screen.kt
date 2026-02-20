package com.example.medjadya

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // หน้าจอสำหรับ Authentication (ไม่ได้อยู่ใน Bottom Nav)
    data object Login : Screen("login_screen", "เข้าสู่ระบบ", Icons.Default.Person)
    data object Register : Screen("register_screen", "สมัครสมาชิก", Icons.Default.Person)

    // หน้าจอหลักใน Bottom Navigation
    data object Home : Screen("home", "หน้าหลัก", Icons.Default.Home)
    data object Medicine : Screen("medicine", "ยา", Icons.Default.List)
    data object Record : Screen("record", "บันทึก", Icons.Default.DateRange)
    data object Profile : Screen("profile_screen", "โปรไฟล์", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Medicine,
    Screen.Record,
    Screen.Profile
)
