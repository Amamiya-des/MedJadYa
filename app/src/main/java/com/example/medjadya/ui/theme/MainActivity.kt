package com.example.medjadya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.medjadya.ui.MainScreen
import com.example.medjadya.ui.theme.MedJadYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedJadYaTheme {
                MainScreen()
            }
        }
    }
}
