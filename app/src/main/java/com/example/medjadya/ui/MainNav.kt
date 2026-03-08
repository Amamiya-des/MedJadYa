package com.example.medjadya.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medjadya.viewmodel.AuthViewModel

@Composable
fun AppNav(start: String) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(context) }
    
    val startDestination = if (start == "home") "profile" else start

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") { LoginScreen(nav, authViewModel) }
        composable("profile") { ProfileScreen(nav, authViewModel) }
    }
}
