package com.example.medjadya.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.medjadya.navigation.Screen
import com.example.medjadya.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavHostController, viewModel: AuthViewModel) {
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") } 
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Log In",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 8.dp)
        )

        Spacer(modifier = Modifier.height(height = 16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 8.dp)
        )

        Spacer(modifier = Modifier.height(height = 32.dp))

        // Login Button
        Button(
            onClick = {
                viewModel.login(email = email, password = password) {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate("main_container") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 56.dp),
            shape = RoundedCornerShape(size = 28.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
        ) {
            Text(text = "Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(height = 24.dp))

        // Register link
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account? ", color = Color.Black)
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text(
                    text = "Register",
                    color = Color(color = 0xFF3F51B5),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
