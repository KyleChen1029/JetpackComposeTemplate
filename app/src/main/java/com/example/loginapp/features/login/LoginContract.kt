package com.example.loginapp.features.login

import com.example.loginapp.core.UiEvent
import com.example.loginapp.core.UiState

// Define the State for the Login Screen
data class LoginScreenState(
    val selectedCountryCode: String = "+886", // Default to Taiwan
    val phoneNumber: String = "",
    val isRememberMeChecked: Boolean = false,
    val currentLanguage: String = "zh-TW", // Default to Traditional Chinese
    val isLoading: Boolean = false,
    val errorMessageKey: String? = null, // Changed from errorMessage
    val errorMessageArgs: List<Any> = emptyList(), // For formatted strings
    val isPhoneNumberValid: Boolean = false // Added for phone validation
) : UiState

// Define Events (User Actions) for the Login Screen
sealed class LoginScreenEvent : UiEvent {
    data object Init : LoginScreenEvent() // Event to load initial data if needed
    data object CountryCodeClicked : LoginScreenEvent()
    data class CountryCodeSelected(val countryCode: String) : LoginScreenEvent()
    data class PhoneNumberChanged(val phoneNumber: String) : LoginScreenEvent()
    data class RememberMeChanged(val isChecked: Boolean) : LoginScreenEvent()
    data object LoginClicked : LoginScreenEvent()
    data class LanguageSelected(val language: String) : LoginScreenEvent()
    // Potentially add an event for phone number focus lost to trigger validation
    data object PhoneNumberFocusLost : LoginScreenEvent()
}
