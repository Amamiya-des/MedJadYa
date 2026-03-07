package com.example.medjadya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.medjadya.data.local.SessionStore
import com.example.medjadya.ui.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val store = remember { SessionStore(ctx) }
            val uid by store.userIdFlow.collectAsState(initial = 0)
            val start = if (uid == 0) "login" else "home"
            AppNav(start)
        }
    }
}
