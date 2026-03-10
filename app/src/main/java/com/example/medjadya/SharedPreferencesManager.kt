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
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_ROLE = "role"
        private const val KEY_REMEMBER_USER = "remember_user"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    fun saveLoginData(token: String, stdId: String, name: String, email: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_STD_ID, stdId)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSavedStdId(): String {
        return preferences.getString(KEY_STD_ID, "") ?: ""
    }

    fun getSavedEmail(): String {
        return preferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun logout(rememberUser: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.remove(KEY_TOKEN)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_ROLE)
        
        if (!rememberUser) {
            editor.remove(KEY_STD_ID)
            editor.remove(KEY_USER_EMAIL)
        }
        editor.putBoolean(KEY_REMEMBER_USER, rememberUser)
        editor.apply()
    }
}
