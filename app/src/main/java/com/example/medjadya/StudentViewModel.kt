package com.example.medjadya

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {
    private var _loginResult by mutableStateOf<LoginClass?>(null)
    val loginResult get() = _loginResult

    private var _studentProfile by mutableStateOf<ProfileClass?>(null)
    val studentProfile get() = _studentProfile

    private var _errorMessage by mutableStateOf("")
    val errorMessage get() = _errorMessage

    fun resetLoginResult() {
        _loginResult = null
    }

    // Login Function ปรับเป็น email และ password
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val loginData = mapOf(
                    "email" to email,
                    "password" to password
                )
                val response = StudentClient.studentAPI.loginStudent(loginData)
                if (response.isSuccessful) {
                    _loginResult = response.body()
                    _errorMessage = ""
                } else {
                    _errorMessage = "Login failed: Invalid email or password"
                }
            } catch (e: Exception) {
                _errorMessage = "Error: ${e.message}"
            }
        }
    }

    fun getProfile(id: String) {
        viewModelScope.launch {
            try {
                val response = StudentClient.studentAPI.getStudentProfile(id)
                if (response.isSuccessful) {
                    _studentProfile = response.body()
                } else {
                    _errorMessage = "User data not found"
                }
            } catch (e: Exception) {
                _errorMessage = "Error: ${e.message}"
            }
        }
    }

    // Register Function
    fun register(context: Context, student: RegisterClass, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = StudentClient.studentAPI.registerStudent(student)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorRawString = response.errorBody()?.string()
                    val finalMessage = if (!errorRawString.isNullOrEmpty()) {
                        try {
                            val errorData = Gson().fromJson(errorRawString, RegisterResponse::class.java)
                            errorData.message
                        } catch (e: Exception) {
                            errorRawString
                        }
                    } else {
                        response.message()
                    }
                    _errorMessage = finalMessage
                    Toast.makeText(context, _errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _errorMessage = "Network Error: ${e.message}"
                Toast.makeText(context, _errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}