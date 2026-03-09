package com.example.medjadya.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session")

class SessionStore(private val context: Context) {
    private val keyUid = intPreferencesKey("uid")
    private val keyName = stringPreferencesKey("name")
    private val keyEmail = stringPreferencesKey("email")

    val userIdFlow = context.dataStore.data.map { it[keyUid] ?: 0 }
    val nameFlow = context.dataStore.data.map { it[keyName] ?: "" }
    val emailFlow = context.dataStore.data.map { it[keyEmail] ?: "" }

    suspend fun save(uid: Int, name: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[keyUid] = uid
            preferences[keyName] = name
            preferences[keyEmail] = email
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
