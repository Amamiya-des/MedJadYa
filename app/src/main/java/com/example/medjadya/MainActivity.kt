package com.example.medjadya

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.medjadya.navigation.NavGraph
import com.example.medjadya.ui.theme.MedJadYaTheme

class MainActivity : ComponentActivity() {
    
    private var navigateToState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle initial intent
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            MedJadYaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val navigateTo by navigateToState
                    
                    NavGraph(
                        navController = navController, 
                        navigateTo = navigateTo,
                        onNavigateHandled = { navigateToState.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("NAVIGATE_TO")?.let {
            navigateToState.value = it
        }
    }
}
