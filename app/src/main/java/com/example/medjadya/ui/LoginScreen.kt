package com.example.medjadya.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var email by rememberSaveable { mutableStateOf(viewModel.getSavedEmail()) }
    var password by rememberSaveable { mutableStateOf("") }

    // Dialog States
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var recoveredPassword by remember { mutableStateOf<String?>(null) }
    var isResetMode by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }

    val primaryColor = Color(0xFF1E9EBD)
    val backgroundColor = Color(0xFFF0F7F9)

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                isResetMode = false // รีเซ็ตสถานะกลับ
                newPasswordInput = ""
            },
            title = {
                Text(
                    text = if (isResetMode) "ตั้งรหัสผ่านใหม่" else "ลืมรหัสผ่าน",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (!isResetMode) {
                        // ขั้นตอนที่ 1: กรอกอีเมล
                        Text(
                            "กรุณากรอกอีเมลเพื่อตรวจสอบบัญชี",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    } else {
                        // ขั้นตอนที่ 2: ตั้งรหัสใหม่ทันที
                        Text(
                            "กรุณากรอกรหัสผ่านใหม่ที่ต้องการ",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text("รหัสผ่านใหม่") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isResetMode) {
                            // ขั้นตอนที่ 1: ตรวจสอบอีเมล
                            if (forgotEmail.isNotEmpty()) {
                                viewModel.forgotPassword(forgotEmail) { success, msg ->
                                    if (success) {
                                        isResetMode = true
                                        // เพิ่ม Toast บอกว่าพบอีเมลแล้ว (ถ้าต้องการ)
                                        Toast.makeText(
                                            context,
                                            "ตรวจสอบอีเมลสำเร็จ กรุณาตั้งรหัสผ่านใหม่",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            // ขั้นตอนที่ 2: ส่งรหัสผ่านใหม่
                            if (newPasswordInput.isNotEmpty()) {
                                viewModel.resetPassword(
                                    forgotEmail,
                                    newPasswordInput
                                ) { success, msg ->
                                    if (success) {
                                        // --- ส่วนที่เพิ่ม/แก้ไข ---
                                        Toast.makeText(
                                            context,
                                            "แก้ไขรหัสผ่านเสร็จแล้ว",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        showForgotDialog = false
                                        isResetMode = false
                                        newPasswordInput = ""
                                    } else {
                                        // กรณีไม่สำเร็จ (เช่น Error 404 หรืออื่นๆ) ให้โชว์ข้อความ error จาก server
                                        Toast.makeText(
                                            context,
                                            "เกิดข้อผิดพลาด: $msg",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "กรุณากรอกรหัสผ่านใหม่", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(if (isResetMode) "ยืนยันการเปลี่ยน" else "ตรวจสอบอีเมล")
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = primaryColor,
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("💊", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MedJadYa",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Text(
            text = "จัดการยาของคุณให้ง่ายขึ้น",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = primaryColor
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = primaryColor
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = {
                forgotEmail = email
                showForgotDialog = true
            }) {
                Text("ลืมรหัสผ่าน?", color = primaryColor, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password) {
                    Toast.makeText(context, "เข้าสู่ระบบสำเร็จ", Toast.LENGTH_SHORT).show()
                    navController.navigate("main_container") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("เข้าสู่ระบบ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ยังไม่มีบัญชี? ", color = Color.Gray)
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text(
                    "ลงทะเบียนที่นี่",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
