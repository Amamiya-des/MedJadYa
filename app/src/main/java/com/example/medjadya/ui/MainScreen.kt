package com.example.medjadya.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medjadya.model.TimeSlot
import com.example.medjadya.navigation.Screen
import com.example.medjadya.navigation.bottomNavItems
import com.example.medjadya.viewmodel.AuthViewModel
import com.example.medjadya.viewmodel.MedicationViewModel
import com.example.medjadya.viewmodel.MedicationViewModelFactory

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    authViewModel: AuthViewModel,
    navigateTo: String? = null,
    onNavigateHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val medicationViewModel: MedicationViewModel = viewModel(
        factory = MedicationViewModelFactory(context)
    )
    val bottomNavController = rememberNavController()

    // Handle inner navigation (e.g. from notification)
    LaunchedEffect(navigateTo) {
        if (navigateTo != null && navigateTo.startsWith("medication_list/")) {
            bottomNavController.navigate(navigateTo) {
                // Ensure we don't build up a massive backstack
                popUpTo(bottomNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            onNavigateHandled()
        }
    }

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
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                MedicationDashboardScreen(
                    viewModel = medicationViewModel,
                    navController = bottomNavController
                )
            }
            composable(Screen.Medicine.route) {
                MedicationScreen()
            }
            composable(Screen.Record.route) {
                val userId = authViewModel.currentUserId.toIntOrNull() ?: 0
                RecordScreen(userId = userId)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = rootNavController, viewModel = authViewModel)
            }
            composable(Screen.Test.route) {
                InsertScreen(navController = bottomNavController, viewModel = medicationViewModel)
            }
            composable(
                route = "medication_list/{slot}",
                arguments = listOf(navArgument("slot") { type = NavType.StringType })
            ) { backStackEntry ->
                val slotName = backStackEntry.arguments?.getString("slot")
                val timeSlot = try {
                    TimeSlot.valueOf(slotName ?: "MORNING")
                } catch (e: Exception) {
                    TimeSlot.MORNING
                }
                MedicationListScreen(
                    timeSlot = timeSlot,
                    viewModel = medicationViewModel,
                    onBack = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}
