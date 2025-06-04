package com.example.loginapp.features.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.AppRoutes
import com.example.loginapp.features.login.LoginViewModel
import com.example.loginapp.features.login.LoginScreenState
import com.example.loginapp.features.login.LoginScreenEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.loginapp.R // Import R class

@OptIn(ExperimentalMaterial3Api::class) // For TextField, Checkbox
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel() // Obtain ViewModel instance
) {
    // Collect state from ViewModel
    // For a more robust solution with lifecycle awareness, consider collectAsStateWithLifecycle
    val uiState by loginViewModel.uiState.collectAsState()

    LoginScreenContent(
        navController = navController,
        uiState = uiState,
        onEvent = loginViewModel::sendEvent // Pass event handler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    navController: NavController,
    uiState: LoginScreenState,
    onEvent: (LoginScreenEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.login_register_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Language Switcher Placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("en")) }) { Text(stringResource(id = R.string.lang_en)) }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("ko")) }) { Text(stringResource(id = R.string.lang_ko)) }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("ja")) }) { Text(stringResource(id = R.string.lang_ja)) }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("zh-TW")) }) { Text(stringResource(id = R.string.lang_zh_tw)) }
        }
        // Display current language from state (for verification)
         Text(stringResource(id = R.string.current_language_debug_label, uiState.currentLanguage), style = MaterialTheme.typography.bodySmall)


        // Account Input Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Country Code Picker Placeholder
            Button(
                onClick = {
                    onEvent(LoginScreenEvent.CountryCodeClicked)
                    navController.navigate(AppRoutes.COUNTRY_CODE_SCREEN)
                },
                modifier = Modifier.wrapContentWidth()
            ) {
                    Text(stringResource(id = R.string.country_code_button_text, uiState.selectedCountryCode))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Phone Number Input
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = { onEvent(LoginScreenEvent.PhoneNumberChanged(it)) },
                    label = { Text(stringResource(id = R.string.phone_number_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = !uiState.isPhoneNumberValid && uiState.phoneNumber.isNotEmpty() // Basic error indication
            )
        }
            if (!uiState.isPhoneNumberValid && uiState.phoneNumber.isNotEmpty() && uiState.errorMessage == null) { // Show generic format error only if no specific error from VM
             Text(
                    text = stringResource(id = R.string.error_invalid_phone_number_format),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )
        }


        // Remember Me Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.End) // Align to the bottom-right of the account field (within this Column scope)
                .padding(top = 8.dp)
        ) {
                Text(stringResource(id = R.string.remember_me_label))
            Checkbox(
                checked = uiState.isRememberMeChecked,
                onCheckedChange = { onEvent(LoginScreenEvent.RememberMeChanged(it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { onEvent(LoginScreenEvent.LoginClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isPhoneNumberValid && !uiState.isLoading // Enable only if phone is valid and not loading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                    Text(stringResource(id = R.string.login_button_text))
            }
        }

        // Display error message from state
            uiState.errorMessageKey?.let { key ->
                val context = LocalContext.current
                val resources = context.resources
                val packageName = context.packageName
                val resId = remember(key) { resources.getIdentifier(key, "string", packageName) }

                val message = if (resId != 0) {
                    if (uiState.errorMessageArgs.isEmpty()) {
                        stringResource(id = resId)
                    } else {
                        stringResource(id = resId, formatArgs = uiState.errorMessageArgs.toTypedArray())
                    }
                } else {
                    key // Fallback to the key itself if resource ID is not found
                }

            Text(
                    text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    // Use a default state for previewing
    val previewState = LoginScreenState(
        selectedCountryCode = "+1",
        phoneNumber = "1234567890",
        isRememberMeChecked = true,
        currentLanguage = "en",
        isLoading = false,
        errorMessage = null,
        isPhoneNumberValid = true
    )
    MaterialTheme { // Wrap with MaterialTheme for preview
        LoginScreenContent(
            navController = rememberNavController(),
            uiState = previewState,
            onEvent = {} // Dummy event handler for preview
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Content - Loading")
@Composable
fun LoginScreenContentLoadingPreview() {
    val previewState = LoginScreenState(isLoading = true, isPhoneNumberValid = true)
    MaterialTheme {
        LoginScreenContent(
            navController = rememberNavController(),
            uiState = previewState,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Content - Error")
@Composable
fun LoginScreenContentErrorPreview() {
        val previewState = LoginScreenState(errorMessageKey = "error_login_failed", isPhoneNumberValid = true)
    MaterialTheme {
        LoginScreenContent(
            navController = rememberNavController(),
            uiState = previewState,
            onEvent = {}
        )
    }
}
