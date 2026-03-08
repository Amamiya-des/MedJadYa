package com.example.medjadya

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("medjadya_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_STD_ID = "std_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ROLE = "role"
    }

    fun saveLoginData(token: String, stdId: String, name: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_STD_ID, stdId)
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun saveLoginStatus(isLoggedIn: Boolean, stdId: String, role: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_STD_ID, stdId)
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun getSavedStdId(): String {
        return preferences.getString(KEY_STD_ID, "") ?: ""
    }

    fun logout(rememberId: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.remove(KEY_TOKEN)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_ROLE)
        if (!rememberId) {
            editor.remove(KEY_STD_ID)
        }
        editor.apply()
    }
}
