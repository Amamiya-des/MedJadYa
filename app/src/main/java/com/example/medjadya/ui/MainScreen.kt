package com.example.medjadya.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medjadya.navigation.Screen
import com.example.medjadya.navigation.bottomNavItems
import com.example.medjadya.viewmodel.AuthViewModel

@Composable
fun MainScreen(rootNavController: NavHostController, authViewModel: AuthViewModel) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { bottomNavController.navigate(Screen.Test.route) },
                containerColor = Color(0xFF1E9EBD),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Test Screen")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
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
            composable(Screen.Record.route) { 
                Surface(modifier = Modifier.fillMaxSize()) { 
                    Text("บันทึก", modifier = Modifier.wrapContentSize()) 
                }
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(navController = rootNavController, viewModel = authViewModel)
            }
            composable(Screen.Test.route) {
                // Assuming TestScreen exists or adding placeholder
                Surface(modifier = Modifier.fillMaxSize()) { 
                    Text("หน้าทดสอบ", modifier = Modifier.wrapContentSize()) 
                }
            }
        }
    }
}
