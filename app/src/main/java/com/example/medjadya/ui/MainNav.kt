package com.example.medjadya.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medjadya.ui.screens.LoginScreen
import com.example.medjadya.ui.screens.ProfileScreen
import com.example.medjadya.ui.screens.RefillAlertsScreen

@Composable
fun AppNav(start: String) {
    val nav = rememberNavController()
    val startDestination = if (start == "home") "profile" else start

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") { LoginScreen(nav) }
        composable("profile") { ProfileScreen(nav) }
        composable("refill") { RefillAlertsScreen(nav) }
    }
}