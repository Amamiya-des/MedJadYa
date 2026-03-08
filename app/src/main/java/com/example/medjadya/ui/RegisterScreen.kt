package com.example.medjadya.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.medjadya.model.RegisterRequest
import com.example.medjadya.navigation.Screen
import com.example.medjadya.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavHostController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(height = 60.dp))
        Text(
            text = "Register",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 8.dp)
        )

        Spacer(modifier = Modifier.height(height = 16.dp))

        // Age
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
            label = { Text(text = "Age") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 8.dp)
        )

        Spacer(modifier = Modifier.height(height = 16.dp))

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

        Spacer(modifier = Modifier.height(height = 40.dp))

        // Register Button
        Button(
            onClick = {
                // Assuming viewModel has a register function that takes RegisterRequest
                Toast.makeText(context, "Registration logic goes here", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 60.dp),
            shape = RoundedCornerShape(size = 30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
            enabled = name.isNotEmpty() && age.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
        ) {
            Text(text = "Register", color = Color.White, fontSize = 18.sp)
        }
    }
}
