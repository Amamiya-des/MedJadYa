package com.example.medjadya.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.medjadya.data.local.SessionStore
import com.example.medjadya.data.api.ApiClient
import com.example.medjadya.data.model.LoginReq
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(nav: NavController) {
    val ctx = LocalContext.current
    val store = remember { SessionStore(ctx) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(Modifier.widthIn(max = 420.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("เข้าสู่ระบบ")
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") })

                Button(
                    onClick = {
                        loading = true
                        scope.launch {
                            try {
                                val res = ApiClient.api.login(LoginReq(email, pass))
                                val u = res.user

                                Toast.makeText(
                                    ctx,
                                    "id=${u.idUser}, name=${u.name}, email=${u.email}",
                                    Toast.LENGTH_LONG
                                ).show()

                                store.save(u.idUser, u.name, u.email)

                                nav.navigate("profile") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading
                ) {
                    Text(if (loading) "กำลังเข้าสู่ระบบ..." else "Login")
                }
            }
        }
    }
}