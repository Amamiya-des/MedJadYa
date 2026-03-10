package com.example.medjadya.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.medjadya.SharedPreferencesManager
import com.example.medjadya.ui.*
import com.example.medjadya.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(context) }
    val sharedPref = SharedPreferencesManager(context)
    
    // Check if user is already logged in
    val startDestination = if (sharedPref.isLoggedIn()) {
        "main_container"
    } else {
        Screen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable("main_container") {
            MainScreen(rootNavController = navController, authViewModel = authViewModel)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController, authViewModel)
        }
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(navController)
        }
        composable(Screen.RefillAlerts.route) {
            RefillAlertsScreen(navController)
        }
    }
}
