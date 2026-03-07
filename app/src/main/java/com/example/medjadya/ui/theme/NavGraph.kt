package com.example.medjadya.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController) {
    val studentViewModel: StudentViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController, studentViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, studentViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, studentViewModel)
        }
    }
}