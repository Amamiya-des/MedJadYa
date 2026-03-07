package com.example.medjadya

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: StudentViewModel) {
    val context = LocalContext.current
    val profile = viewModel.studentProfile?.data

    // State สำหรับเก็บค่าที่แก้ไข
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var age by remember { mutableStateOf(profile?.age?.toString() ?: "") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "แก้ไขโปรไฟล์", 
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0097B2)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE1F5FE))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // idUser (Disabled/Read-only)
            OutlinedTextField(
                value = profile?.idUser?.toString() ?: "",
                onValueChange = {},
                label = { Text("รหัสผู้ใช้ (แก้ไขไม่ได้)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.DarkGray,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledLeadingIconColor = Color.Gray,
                    disabledContainerColor = Color(0xFFF0F0F0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ชื่อ") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0097B2),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                label = { Text("อายุ") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0097B2),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("อีเมล") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0097B2),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && age.isNotEmpty()) {
                        profile?.idUser?.let { id ->
                            // ส่งค่า age ไปด้วย
                            viewModel.updateProfile(id.toString(), name, email, age) {
                                Toast.makeText(context, "บันทึกการแก้ไข", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() 
                            }
                        }
                    } else {
                        Toast.makeText(context, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097B2))
            ) {
                Text(
                    "บันทึกการเปลี่ยนแปลง", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
