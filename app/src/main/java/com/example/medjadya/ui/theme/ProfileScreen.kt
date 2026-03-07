package com.example.medjadya.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: StudentViewModel) {
    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)
    val profile = viewModel.studentProfile.value
    val errorMsg = viewModel.errorMessage.value

    var showDialog by remember { mutableStateOf(false) }
    var rememberId by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        val savedId = sharedPref.getSavedUserId()
        if (savedId.isNotEmpty()) {
            viewModel.getProfile(savedId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Profile",
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            profile?.let {
                ProfileTextItem(text = "User ID: ${it.idUser}")
                ProfileTextItem(text = "Name: ${it.name}")
                ProfileTextItem(text = "Age: ${it.age}")
                ProfileTextItem(text = "Email: ${it.email}")
            } ?: run {
                Text(
                    text = if (errorMsg.isNotEmpty()) errorMsg else "Loading...",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text(text = "Logout", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Logout") },
            text = {
                Column {
                    Text("Do you want to sign out?")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberId,
                            onCheckedChange = { rememberId = it }
                        )
                        Text("Remember my User ID")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sharedPref.logout(rememberId)
                        showDialog = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileTextItem(text: String) {
    Text(
        text = text,
        fontSize = 22.sp,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}
