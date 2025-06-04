package com.example.loginapp.features.login

import android.content.Context // Required for UserPreferencesRepository instantiation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.loginapp.core.BaseViewModel
import com.example.loginapp.data.UserPreferencesRepository
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : BaseViewModel<LoginScreenState, LoginScreenEvent>() {

        public override val initialState: LoginScreenState = LoginScreenState()
    private var phoneNumberUtil: PhoneNumberUtil? = null

    init {
        // Trigger loading of preferences when ViewModel is created
        sendEvent(LoginScreenEvent.Init)
    }

    fun initializePhoneNumberUtil(util: PhoneNumberUtil) {
        if (phoneNumberUtil == null) {
            phoneNumberUtil = util
        }
    }

    override fun handleEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.Init -> {
                viewModelScope.launch {
                    val prefs = userPreferencesRepository.userPreferencesFlow.first()
                    setState {
                        copy(
                            selectedCountryCode = prefs.countryCode,
                            phoneNumber = if (prefs.rememberMe) prefs.phoneNumber else "",
                            isRememberMeChecked = prefs.rememberMe,
                            currentLanguage = prefs.language, // Load language
                            // Reset validation state, phone number might need re-validation if loaded
                            isPhoneNumberValid = false,
                            errorMessageKey = null
                        )
                    }
                    // If phone number is loaded and remember me is checked, validate it
                    if (prefs.rememberMe && uiState.value.phoneNumber.isNotEmpty()) {
                        validatePhoneNumber(uiState.value.selectedCountryCode, uiState.value.phoneNumber)
                    }
                    // MainActivity will observe uiState.currentLanguage and recreate if necessary.
                }
            }
            is LoginScreenEvent.CountryCodeSelected -> {
                setState {
                    copy(
                        selectedCountryCode = event.countryCode,
                        phoneNumber = "",
                        isPhoneNumberValid = false,
                        errorMessageKey = null
                    )
                }
            }
            is LoginScreenEvent.PhoneNumberChanged -> {
                setState { copy(phoneNumber = event.phoneNumber, errorMessageKey = null) }
                validatePhoneNumber(uiState.value.selectedCountryCode, event.phoneNumber)
            }
            is LoginScreenEvent.PhoneNumberFocusLost -> {
                validatePhoneNumber(uiState.value.selectedCountryCode, uiState.value.phoneNumber)
            }
            is LoginScreenEvent.RememberMeChanged -> {
                val isChecked = event.isChecked
                setState { copy(isRememberMeChecked = isChecked) }
                viewModelScope.launch {
                    userPreferencesRepository.updateRememberMe(isChecked)
                    if (isChecked) {
                        // If checked, and current phone number is valid, save it.
                        if(uiState.value.isPhoneNumberValid){
                            userPreferencesRepository.saveLoginDetails(
                                uiState.value.selectedCountryCode,
                                uiState.value.phoneNumber
                            )
                        }
                    }
                    // No need to clear fields from state here, updateRememberMe handles DataStore
                }
            }
            is LoginScreenEvent.LoginClicked -> {
                validatePhoneNumber(uiState.value.selectedCountryCode, uiState.value.phoneNumber, true)
                if (uiState.value.isPhoneNumberValid) {
                    viewModelScope.launch {
                        if (uiState.value.isRememberMeChecked) {
                            userPreferencesRepository.saveLoginDetails(
                                uiState.value.selectedCountryCode,
                                uiState.value.phoneNumber
                            )
                        } else {
                            // Ensure details are cleared if "Remember Me" was unchecked during this session
                            // updateRememberMe(false) already clears them from DataStore.
                            // If it was checked, then unchecked, then login, this ensures it's not saved.
                            // Explicitly ensuring DataStore reflects "not remembering" if it's unchecked now.
                            userPreferencesRepository.updateRememberMe(false)
                        }
                    }
                    performLogin()
                } else {
                     if (uiState.value.errorMessageKey == null && uiState.value.phoneNumber.isNotEmpty()) {
                        setState { copy(errorMessageKey = "error_invalid_phone_number_format") }
                    } else if (uiState.value.errorMessageKey == null && uiState.value.phoneNumber.isEmpty()) {
                        setState { copy(errorMessageKey = "error_phone_cannot_be_empty") }
                    }
                }
            }
            is LoginScreenEvent.LanguageSelected -> {
                val newLanguage = event.language
                setState { copy(currentLanguage = newLanguage) }
                viewModelScope.launch {
                    userPreferencesRepository.saveLanguage(newLanguage)
                }
            }
            is LoginScreenEvent.CountryCodeClicked -> { /* No state change, handled by UI */ }
        }
    }

    private fun validatePhoneNumber(countryDialCode: String, number: String, isSubmitting: Boolean = false) {
        if (phoneNumberUtil == null) {
            val isValidSimple = number.isNotBlank() && number.length >= 6
            setState { copy(isPhoneNumberValid = isValidSimple) }
            if(isSubmitting && !isValidSimple && number.isNotEmpty()){
                setState { copy(errorMessageKey = "error_phone_not_verifiable")}
            } else if (isSubmitting && number.isEmpty()){
                 setState { copy(errorMessageKey = "error_phone_cannot_be_empty")}
            }
            return
        }
        if (number.isBlank()) {
            // Only show "cannot be empty" error if submitting, otherwise just mark as invalid
            setState { copy(isPhoneNumberValid = false, errorMessageKey = if (isSubmitting) "error_phone_cannot_be_empty" else null) }
            return
        }
        try {
            val dialCodeAsInt = countryDialCode.removePrefix("+").toIntOrNull()
            var regionCode: String? = null
            if(dialCodeAsInt != null) {
                regionCode = phoneNumberUtil?.getRegionCodeForCountryCode(dialCodeAsInt)
            }
            if (regionCode != null) {
                val phoneNumberProto = phoneNumberUtil!!.parse(number, regionCode)
                val isValid = phoneNumberUtil!!.isValidNumber(phoneNumberProto)
                setState { copy(isPhoneNumberValid = isValid, errorMessageKey = if (!isValid && isSubmitting) "error_invalid_phone_for_region" else if (isValid) null else uiState.value.errorMessageKey) }
            } else {
                val isValidSimple = number.length >= 7
                setState { copy(isPhoneNumberValid = isValidSimple, errorMessageKey = if (!isValidSimple && isSubmitting) "error_region_not_determinable" else if (isValidSimple) null else uiState.value.errorMessageKey) }
            }
        } catch (e: Exception) {
            setState { copy(isPhoneNumberValid = false, errorMessageKey = if (isSubmitting) "error_parsing_phone_number" else null) }
        }
    }

    private fun performLogin() {
        setState { copy(isLoading = true, errorMessageKey = null) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val isLoginSuccess = uiState.value.phoneNumber.hashCode() % 2 == 0
            if (isLoginSuccess) {
                setState { copy(isLoading = false, errorMessageKey = null) }
            } else {
                setState { copy(isLoading = false, errorMessageKey = "error_login_failed") }
            }
        }
    }
}

// Factory for LoginViewModel
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(UserPreferencesRepository(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
