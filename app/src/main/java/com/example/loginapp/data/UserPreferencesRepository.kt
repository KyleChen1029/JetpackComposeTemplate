package com.example.loginapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define DataStore instance at the top level (Context extension)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val SAVED_COUNTRY_CODE_KEY = stringPreferencesKey("saved_country_code")
        val SAVED_PHONE_NUMBER_KEY = stringPreferencesKey("saved_phone_number")
        // Add language key if we want to persist language preference
        val SAVED_LANGUAGE_KEY = stringPreferencesKey("saved_language")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val rememberMe = preferences[REMEMBER_ME_KEY] ?: false
            val countryCode = preferences[SAVED_COUNTRY_CODE_KEY] ?: "+886" // Default
            val phoneNumber = preferences[SAVED_PHONE_NUMBER_KEY] ?: ""
            val language = preferences[SAVED_LANGUAGE_KEY] ?: "zh-TW" // Default language
            UserPreferences(rememberMe, countryCode, phoneNumber, language)
        }

    suspend fun updateRememberMe(rememberMe: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = rememberMe
            // If not remembering, clear saved credentials
            if (!rememberMe) {
                preferences.remove(SAVED_COUNTRY_CODE_KEY)
                preferences.remove(SAVED_PHONE_NUMBER_KEY)
            }
        }
    }

    suspend fun saveLoginDetails(countryCode: String, phoneNumber: String) {
        context.dataStore.edit { preferences ->
            // Only save if remember me is true (though viewmodel can also check this)
             if (preferences[REMEMBER_ME_KEY] == true) {
                preferences[SAVED_COUNTRY_CODE_KEY] = countryCode
                preferences[SAVED_PHONE_NUMBER_KEY] = phoneNumber
            }
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_LANGUAGE_KEY] = languageCode
        }
    }
}

data class UserPreferences(
    val rememberMe: Boolean,
    val countryCode: String,
    val phoneNumber: String,
    val language: String
)
