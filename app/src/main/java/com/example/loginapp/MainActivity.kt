package com.example.loginapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Needed for Preview's ViewModel Factory
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider // Add this
import androidx.lifecycle.viewmodel.compose.viewModel // For accessing ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.core.LocaleHelper
import com.example.loginapp.features.login.LoginViewModel
import com.example.loginapp.features.login.LoginViewModelFactory // Add this
import com.example.loginapp.features.login.ui.LoginScreen
import com.example.loginapp.features.countrycode.ui.CountryCodeScreen
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.Locale

object AppRoutes {
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_CODE_SCREEN = "country_code"
}

class MainActivity : ComponentActivity() {

    private var currentLanguage: String = getDefaultLanguage()

    private fun getDefaultLanguage(): String {
        return "zh-TW"
    }

    override fun attachBaseContext(newBase: Context) {
        val contextToUse = LocaleHelper.setLocale(newBase, currentLanguage)
        super.attachBaseContext(contextToUse)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumberUtil = PhoneNumberUtil.createInstance(applicationContext)

        setContent {
            val factory = LoginViewModelFactory(applicationContext)
            val loginViewModel: LoginViewModel = viewModel(factory = factory)

            LaunchedEffect(key1 = Unit) {
                loginViewModel.initializePhoneNumberUtil(phoneNumberUtil)
                if (loginViewModel.uiState.value.currentLanguage != currentLanguage) {
                    loginViewModel.sendEvent(com.example.loginapp.features.login.LoginScreenEvent.LanguageSelected(currentLanguage))
                }
            }

            val appState by loginViewModel.uiState.collectAsState()

            LaunchedEffect(appState.currentLanguage) {
                if (currentLanguage != appState.currentLanguage) {
                    currentLanguage = appState.currentLanguage
                    recreate()
                }
            }

            LoginAppTheme(key = appState.currentLanguage) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(loginViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(loginViewModel: LoginViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppRoutes.LOGIN_SCREEN) {
        composable(AppRoutes.LOGIN_SCREEN) {
            LoginScreen(navController = navController, loginViewModel = loginViewModel)
        }
        composable(AppRoutes.COUNTRY_CODE_SCREEN) {
            CountryCodeScreen(navController = navController, loginViewModel = loginViewModel)
        }
    }
}

@Composable
fun LoginAppTheme(key: Any? = null, content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val factory = LoginViewModelFactory(LocalContext.current.applicationContext)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    LoginAppTheme {
        AppNavigation(loginViewModel)
    }
}
