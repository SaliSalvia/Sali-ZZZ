package com.salizzai.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sali_zzai_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val MODEL = stringPreferencesKey("model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val THINKING_ENABLED = booleanPreferencesKey("thinking_enabled")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME] ?: "dark" }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "fa" }
    val model: Flow<String> = context.dataStore.data.map { it[MODEL] ?: "glm-4.6" }
    val temperature: Flow<Float> = context.dataStore.data.map { it[TEMPERATURE] ?: 0.7f }
    val thinkingEnabled: Flow<Boolean> = context.dataStore.data.map { it[THINKING_ENABLED] ?: false }
    val systemPrompt: Flow<String> = context.dataStore.data.map { it[SYSTEM_PROMPT] ?: "" }
    val loggedInUserId: Flow<String?> = context.dataStore.data.map { it[LOGGED_IN_USER_ID] }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[THEME] = value }
    }
    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[LANGUAGE] = value }
    }
    suspend fun setModel(value: String) {
        context.dataStore.edit { it[MODEL] = value }
    }
    suspend fun setTemperature(value: Float) {
        context.dataStore.edit { it[TEMPERATURE] = value }
    }
    suspend fun setThinkingEnabled(value: Boolean) {
        context.dataStore.edit { it[THINKING_ENABLED] = value }
    }
    suspend fun setSystemPrompt(value: String) {
        context.dataStore.edit { it[SYSTEM_PROMPT] = value }
    }
    suspend fun setLoggedInUserId(value: String?) {
        context.dataStore.edit {
            if (value == null) it.remove(LOGGED_IN_USER_ID) else it[LOGGED_IN_USER_ID] = value
        }
    }
}
