package com.example.medjadya.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.medjadya.SharedPreferencesManager
import com.example.medjadya.ui.*
import com.example.medjadya.viewmodel.*

@Composable
fun NavGraph(
    navController: NavHostController, 
    navigateTo: String? = null,
    onNavigateHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(context) }
    val sharedPref = SharedPreferencesManager(context)
    
    // Check if user is already logged in
    val startDestination = if (sharedPref.isLoggedIn()) {
        "main_container"
    } else {
        Screen.Login.route
    }

    // Navigation logic handled here
    LaunchedEffect(navigateTo) {
        if (navigateTo != null) {
            if (navigateTo.startsWith("medication_list/")) {
                // If it's an inner route, we stay on main_container 
                // and pass the navigation request down to MainScreen
                if (navController.currentDestination?.route != "main_container") {
                    navController.navigate("main_container") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
                // We don't call onNavigateHandled() yet because MainScreen 
                // needs to see it to do the inner navigation.
            } else {
                // Direct root level navigation
                navController.navigate(navigateTo) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
                onNavigateHandled()
            }
        }
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
            MainScreen(
                rootNavController = navController, 
                authViewModel = authViewModel,
                navigateTo = navigateTo,
                onNavigateHandled = onNavigateHandled
            )
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
