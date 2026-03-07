package com.example.medjadya.ui.theme

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("student_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "role"
    }

    // Login
    fun saveLoginStatus(isLoggedIn: Boolean, userId: String, role: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)
        editor.apply() // Save data
    }

    // set user_ID
    fun getSavedUserId(): String {
        return preferences.getString(KEY_USER_ID, "") ?: ""
    }

    // Logout
    fun logout(rememberId: Boolean) {
        val editor = preferences.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_ROLE)
        if (!rememberId) {
            editor.remove(KEY_USER_ID)
        }
        editor.apply()
    }
}