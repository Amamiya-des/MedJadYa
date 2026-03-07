package com.example.medjadya.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("session")

class SessionStore(private val context: Context) {
  private val KEY_UID = intPreferencesKey("uid")
  private val KEY_NAME = stringPreferencesKey("name")
  private val KEY_EMAIL = stringPreferencesKey("email")

  val userIdFlow = context.ds.data.map { it[KEY_UID] ?: 0 }
  val nameFlow = context.ds.data.map { it[KEY_NAME] ?: "" }
  val emailFlow = context.ds.data.map { it[KEY_EMAIL] ?: "" }

  suspend fun save(uid: Int, name: String, email: String) {
    context.ds.edit {
      it[KEY_UID] = uid
      it[KEY_NAME] = name
      it[KEY_EMAIL] = email
    }
  }
  suspend fun clear() { context.ds.edit { it.clear() } }
}
