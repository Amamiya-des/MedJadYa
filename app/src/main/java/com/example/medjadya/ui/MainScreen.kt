package com.example.medjadya.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medjadya.model.MedLog
import com.example.medjadya.model.Medication
import com.example.medjadya.navigation.Screen
import com.example.medjadya.navigation.bottomNavItems
import com.example.medjadya.viewmodel.MedLogViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E9EBD),
                            selectedTextColor = Color(0xFF1E9EBD),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE0F2F1)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Medicine.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                Surface(modifier = Modifier.fillMaxSize()) { 
                    Text("หน้าหลัก", modifier = Modifier.wrapContentSize()) 
                }
            }
            composable(Screen.Medicine.route) { 
                MedicationScreen() 
            }
            composable(Screen.Record.route) {backStackEntry ->
                // การเรียก viewModel() ตรงนี้จะทำให้ ViewModel มีอายุเท่ากับหน้านี้
                val medicationViewModel: MedLogViewModel= viewModel()

                RecordScreen(
                    userId = 2, viewModel = medicationViewModel
                )
            }
            composable(Screen.Profile.route) { 
                Surface(modifier = Modifier.fillMaxSize()) { 
                    Text("โปรไฟล์", modifier = Modifier.wrapContentSize()) 
                }
            }
        }
    }
}
