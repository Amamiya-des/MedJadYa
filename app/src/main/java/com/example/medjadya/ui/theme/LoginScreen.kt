package com.example.medjadya.ui.theme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LoginScreen(navController: NavHostController, viewModel: StudentViewModel) {
    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginResult = viewModel.loginResult.value

    LaunchedEffect(key1 = loginResult) {
        loginResult?.let {
            if (!it.error) {
                sharedPref.saveLoginStatus(isLoggedIn = true, userId = it.idUser.toString(), role = "")
                viewModel.resetLoginResult()
                Toast.makeText(context, "เข้าสู่ระบบสำเร็จ! ยินดีต้อนรับ ${it.name}", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                viewModel.resetLoginResult()
                Toast.makeText(context, it.message ?: "เข้าสู่ระบบล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF89E8FF), Color(0xFFC9FFFA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = "Med Judya",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("อีเมลหรือชื่อผู้ใช้", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("รหัสผ่าน", color = Color.Gray) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Black)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(email = email, password = password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097C1)),
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(text = "ลงชื่อเข้าใช้", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text(text = "สมัครสมาชิกใหม่", color = Color(0xFF0097C1), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "@ITKKU10",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}