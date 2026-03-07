package com.example.medjadya

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    // SharedPreferences File
    private val preferences: SharedPreferences =
        context.getSharedPreferences("student_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_STD_ID = "std_id"
        private const val KEY_ROLE = "role"
    }

    // Login
    fun saveLoginStatus(isLoggedIn: Boolean, stdId: String, role: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_STD_ID, stdId)
        editor.putString(KEY_ROLE, role)
        editor.apply() // Save data
    }

    // get std_ID
    fun getSavedStdId(): String {
        return preferences.getString(KEY_STD_ID, "") ?: ""
    }

    // Logout
    fun logout(rememberId: Boolean) {
        val editor = preferences.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_ROLE)
        if (!rememberId) {
            editor.remove(KEY_STD_ID)
        }
        editor.apply()
    }
}