package com.example.medjadya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medjadya.data.model.TimeSlot
import com.example.medjadya.ui.screens.MedicationDashboardScreen
import com.example.medjadya.ui.screens.MedicationListScreen
import com.example.medjadya.ui.screens.MedicationViewModel
import com.example.medjadya.ui.theme.MedJadYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedJadYaTheme {
                // บังคับพื้นหลังขาวครอบคลุมทั้งแอป
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val medicationViewModel: MedicationViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            MedicationDashboardScreen(
                viewModel = medicationViewModel,
                navController = navController // แก้ไข: ส่ง navController เข้าไปเพื่อให้ Screen ใช้งานได้
            )
        }
        composable(
            route = "medication_list/{slot}",
            arguments = listOf(navArgument("slot") { type = NavType.StringType })
        ) { backStackEntry ->
            val slotStr = backStackEntry.arguments?.getString("slot") ?: "MORNING"
            val slot = try { TimeSlot.valueOf(slotStr) } catch (e: Exception) { TimeSlot.MORNING }
            
            MedicationListScreen(
                timeSlot = slot,
                viewModel = medicationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
