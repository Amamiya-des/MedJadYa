package com.example.medjadya.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "หน้าหลัก", Icons.Default.Home)
    object Medicine : Screen("medicine", "ยา", Icons.Default.List)
    object Record : Screen("record", "บันทึก", Icons.Default.DateRange)
    object Profile : Screen("profile", "โปรไฟล์", Icons.Default.Person)
    object Test : Screen("test", "ทดสอบ", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Medicine,
    Screen.Record,
    Screen.Profile
)
