package com.example.loginapp

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.core.LocaleHelper
import com.example.loginapp.features.login.LoginViewModel
import com.example.loginapp.features.login.LoginViewModelFactory
import com.example.loginapp.features.login.ui.LoginScreen // Ensure correct import
import com.example.loginapp.features.countrycode.ui.CountryCodeScreen // Ensure correct import
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.Locale

object AppRoutes {
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_CODE_SCREEN = "country_code"
}

class MainActivity : ComponentActivity() {

    private var currentLanguage: String = "zh-TW"
    private lateinit var loginViewModel: LoginViewModel

    override fun attachBaseContext(newBase: Context) {
        Log.d("MainActivity", "attachBaseContext: currentLanguage before setLocale = $currentLanguage")
        super.attachBaseContext(LocaleHelper.setLocale(newBase, currentLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Start, currentLanguage = $currentLanguage")

        val factory = LoginViewModelFactory(applicationContext)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        val phoneNumberUtil = PhoneNumberUtil.createInstance(applicationContext)
        loginViewModel.initializePhoneNumberUtil(phoneNumberUtil)

        setContent {
            // Provide explicit initial value to collectAsState
            val appState by loginViewModel.uiState.collectAsState(initial = loginViewModel.initialState)

            Log.d("MainActivity", "setContent: Composing with ViewModel's language: ${appState.currentLanguage}. MainActivity's currentLanguage: $currentLanguage")

            LaunchedEffect(appState.currentLanguage) {
                Log.d("MainActivity", "LaunchedEffect: ViewModel language is ${appState.currentLanguage}, Activity language is $currentLanguage")
                if (currentLanguage != appState.currentLanguage) {
                    Log.d("MainActivity", "LaunchedEffect: Language mismatch. Updating Activity language to ${appState.currentLanguage} and recreating.")
                    currentLanguage = appState.currentLanguage
                    this@MainActivity.recreate()
                } else {
                    Log.d("MainActivity", "LaunchedEffect: Languages match. No recreation needed based on language.")
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
        Log.d("MainActivity", "onCreate: End")
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
    val context = LocalContext.current
    val factory = LoginViewModelFactory(context.applicationContext)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    LoginAppTheme {
        AppNavigation(loginViewModel)
    }
}
