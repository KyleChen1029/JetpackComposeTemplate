package com.example.loginapp.features.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val uiState by loginViewModel.uiState.collectAsState()

    LoginScreenContent(
        navController = navController,
        uiState = uiState,
        onEvent = loginViewModel::sendEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    navController: NavController,
    uiState: LoginScreenState,
    onEvent: (LoginScreenEvent) -> Unit
) {
    // Language Switcher Placeholder - ensure it's at the top right of the screen area
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, end = 8.dp, bottom = 16.dp), // Adjusted padding
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("en")) }) { Text(stringResource(id = R.string.lang_en)) }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("ko")) }) { Text(stringResource(id = R.string.lang_ko)) }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("ja")) }) { Text(stringResource(id = R.string.lang_ja)) }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { onEvent(LoginScreenEvent.LanguageSelected("zh-TW")) }) { Text(stringResource(id = R.string.lang_zh_tw)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp) // More horizontal padding
            .imePadding() // Handles keyboard overlap
            .verticalScroll(rememberScrollState()), // Add scrolling for smaller screens
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.login_register_title),
            style = MaterialTheme.typography.headlineLarge, // Larger title
            modifier = Modifier.padding(bottom = 48.dp) // More space below title
        )

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
                modifier = Modifier.defaultMinSize(minWidth = 100.dp), // Give it some min width
                shape = MaterialTheme.shapes.medium // M3 shape
            ) {
                    Text(stringResource(id = R.string.country_code_button_text, uiState.selectedCountryCode))
            }

            Spacer(modifier = Modifier.width(12.dp)) // Increased spacer

            // Phone Number Input
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = { onEvent(LoginScreenEvent.PhoneNumberChanged(it)) },
                label = { Text(stringResource(id = R.string.phone_number_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = !uiState.isPhoneNumberValid && uiState.phoneNumber.isNotEmpty(), // Basic error indication
                textStyle = MaterialTheme.typography.bodyLarge, // Ensure text size is good
                shape = MaterialTheme.shapes.medium // M3 shape
            )
        }
        // Corrected: Use errorMessageKey
        if (!uiState.isPhoneNumberValid && uiState.phoneNumber.isNotEmpty() && uiState.errorMessageKey == null) { // Show generic format error only if no specific error from VM
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
                .fillMaxWidth() // Take full width to align checkbox to the right
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End // Align content (Text + Checkbox) to the right
        ) {
            Text(
                text = stringResource(id = R.string.remember_me_label),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp)) // Small space
            Checkbox(
                checked = uiState.isRememberMeChecked,
                onCheckedChange = { onEvent(LoginScreenEvent.RememberMeChanged(it)) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary) // M3 colors
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Increased spacer

        // Login Button
        Button(
            onClick = { onEvent(LoginScreenEvent.LoginClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // Standard button height
            enabled = uiState.isPhoneNumberValid && !uiState.isLoading,
            shape = MaterialTheme.shapes.medium // M3 shape
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp // Slightly thicker
                )
            } else {
                Text(stringResource(id = R.string.login_button_text), style = MaterialTheme.typography.labelLarge)
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
                modifier = Modifier.padding(top = 24.dp) // More space for error
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
        errorMessageKey = null, // Corrected
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
