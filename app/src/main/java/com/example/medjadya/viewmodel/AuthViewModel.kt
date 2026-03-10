package com.example.medjadya.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.SharedPreferencesManager
import com.example.medjadya.data.repository.AuthRepository
import com.example.medjadya.model.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val repository = AuthRepository(appContext)
    private val sharedPref = SharedPreferencesManager(appContext)

    var authResponse by mutableStateOf<AuthResponse?>(null)
        private set

    var userProfile by mutableStateOf<UserData?>(null)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var currentUserId by mutableStateOf(sharedPref.getSavedStdId())
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.token != null) {
                        val userId = body.idUser?.toString() ?: decodeUserId(body.token) ?: ""

                        sharedPref.saveLoginData(
                            token = body.token,
                            stdId = userId,
                            name = body.name ?: "",
                            email = email
                        )
                        currentUserId = userId
                        authResponse = body
                        errorMessage = ""
                        onSuccess()
                    } else {
                        errorMessage = "ไม่ได้รับ Token จากระบบ"
                    }
                } else {
                    errorMessage = "เข้าสู่ระบบไม่สำเร็จ: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "เกิดข้อผิดพลาด: ${e.message}"
                Log.e("AuthViewModel", "Login error", e)
            }
        }
    }

    fun forgotPassword(email: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email)
                if (response.isSuccessful) {
                    val body = response.body()
                    // สำคัญ: เก็บค่า response ไว้เพื่อให้ UI ดึง password ไปแสดงได้
                    authResponse = body
                    onComplete(true, body?.message ?: "ตรวจสอบข้อมูลสำเร็จ")
                } else {
                    onComplete(false, "ไม่พบอีเมลนี้ในระบบ")
                }
            } catch (e: Exception) {
                onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // ส่งค่าแยกกัน 2 ตัวตามที่ Repository รับ
                val response = repository.resetPassword(email, newPassword)

                if (response.isSuccessful) {
                    onComplete(true, "เปลี่ยนรหัสผ่านสำเร็จแล้ว")
                } else {
                    // ถ้า Error 404 จะมาตกที่นี่
                    onComplete(false, "เกิดข้อผิดพลาดจาก Server (404/500): ${response.code()}")
                }
            } catch (e: Exception) {
                onComplete(false, "เชื่อมต่อไม่ได้: ${e.localizedMessage}")
            }
        }
    }

    private fun decodeUserId(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
                val json = JSONObject(payload)
                when {
                    json.has("idUser") -> json.getString("idUser")
                    json.has("id") -> json.getString("id")
                    json.has("userId") -> json.getString("userId")
                    json.has("sub") -> json.getString("sub")
                    else -> null
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun fetchProfile(id: String) {
        if (id.isEmpty()) return

        viewModelScope.launch {
            try {
                val response = repository.getUserProfile(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        userProfile = body
                        errorMessage = ""
                    } else {
                        errorMessage = "ข้อมูลโปรไฟล์ว่างเปล่า"
                    }
                } else {
                    errorMessage = "เซิร์ฟเวอร์ตอบกลับผิดพลาด: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้"
                Log.e("AuthViewModel", "fetchProfile error for ID: $id", e)
            }
        }
    }

    fun updateProfile(
        context: Context,
        id: String,
        name: String,
        email: String,
        age: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userData = mapOf(
                    "name" to name,
                    "email" to email,
                    "age" to age
                )
                val response = repository.updateProfile(id, userData)
                if (response.isSuccessful) {
                    fetchProfile(id)
                    onSuccess()
                } else {
                    errorMessage = "แก้ไขโปรไฟล์ไม่สำเร็จ: ${response.message()}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                errorMessage = "เกิดข้อผิดพลาด: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logout(rememberUser: Boolean) {
        sharedPref.logout(rememberUser)
        userProfile = null
        authResponse = null
        if (!rememberUser) {
            currentUserId = ""
        }
        errorMessage = ""
    }

    fun register(name: String, age: Int, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.register(RegisterRequest(name, age, email, password))
                if (response.isSuccessful) {
                    errorMessage = ""
                    onSuccess()
                } else {
                    errorMessage = "ลงทะเบียนไม่สำเร็จ: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "เกิดข้อผิดพลาดในการลงทะเบียน: ${e.message}"
            }
        }
    }

    fun getSavedEmail(): String = sharedPref.getSavedEmail()
}
