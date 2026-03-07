package com.example.medjadya.ui.theme

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {
    private var _loginResult = mutableStateOf<LoginClass?>(null)
    val loginResult get() = _loginResult

    private var _studentProfile = mutableStateOf<ProfileData?>(null)
    val studentProfile get() = _studentProfile

    private var _errorMessage = mutableStateOf("")
    val errorMessage get() = _errorMessage

    fun resetLoginResult() {
        _loginResult.value = null
    }

    // Login Function
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val loginData = mapOf(
                    "email" to email,
                    "password" to password
                )
                val response = StudentClient.studentAPI.loginStudent(loginData)
                if (response.isSuccessful) {
                    _loginResult.value = response.body()
                    _errorMessage.value = ""
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, LoginClass::class.java)
                    _errorMessage.value = errorResponse?.message ?: "Login failed: Invalid credentials"
                    _loginResult.value = LoginClass(true, _errorMessage.value, null, null, null, null)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun getProfile(id: String) {
        viewModelScope.launch {
            try {
                val response = StudentClient.studentAPI.getstudentProfile(id)
                if (response.isSuccessful && response.body() != null) {
                    _studentProfile.value = response.body()!!.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Student data not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun register(context: Context, student: RegisterClass, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = StudentClient.studentAPI.registerStudent(studentData = student)
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
                    _errorMessage.value = finalMessage
                    Toast.makeText(context, _errorMessage.value, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network Error: ${e.message}"
                Toast.makeText(context, _errorMessage.value, Toast.LENGTH_SHORT).show()
            }
        }
    }
}