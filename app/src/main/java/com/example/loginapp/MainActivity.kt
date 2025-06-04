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
import com.example.loginapp.features.login.ui.LoginScreen
import com.example.loginapp.features.countrycode.ui.CountryCodeScreen
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.Locale // Ensure this is imported

// AppRoutes object should be here or accessible (already defined in previous steps)
object AppRoutes {
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_CODE_SCREEN = "country_code"
}

class MainActivity : ComponentActivity() {

    private var currentLanguage: String = Locale.getDefault().language // Initialize with system default or a constant
    private lateinit var loginViewModel: LoginViewModel // To hold ViewModel instance

    override fun attachBaseContext(newBase: Context) {
        // Load saved language from UserPreferencesRepository synchronously for attachBaseContext if possible,
        // or use a reliable default. For simplicity, we'll use the 'currentLanguage' property,
        // which will be updated by the ViewModel.
        // The critical part is that after ViewModel loads, it will trigger a recreate if necessary.
        Log.d("MainActivity", "attachBaseContext: currentLanguage = $currentLanguage")
        super.attachBaseContext(LocaleHelper.setLocale(newBase, currentLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Start")

        val factory = LoginViewModelFactory(applicationContext)
        // Initialize ViewModel using ViewModelProvider for Activity scope
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        val phoneNumberUtil = PhoneNumberUtil.createInstance(applicationContext)
        loginViewModel.initializePhoneNumberUtil(phoneNumberUtil) // Ensure this is called

        // Initial load of language from ViewModel's state (which loads from DataStore)
        // This helps sync MainActivity's currentLanguage with persisted preference ASAP.
        // loginViewModel.uiState.value should reflect the state after Init event (which loads DataStore)
        // because BaseViewModel initializes state eagerly.
        // However, the Init event in LoginViewModel is asynchronous (viewModelScope.launch).
        // So, the very first value of uiState.value.currentLanguage might be the default from LoginScreenState,
        // not the one from DataStore yet.
        // The LaunchedEffect observing appState.currentLanguage is more reliable for reacting to DataStore loaded language.

        // Forcing an initial update of currentLanguage from ViewModel's *initial* default if they differ.
        // This is before DataStore might have updated the ViewModel's state.
        if (currentLanguage != loginViewModel.uiState.value.currentLanguage) {
             Log.d("MainActivity", "onCreate: Initial language sync. VM default: ${loginViewModel.uiState.value.currentLanguage}, Activity: $currentLanguage.")
             // If we set currentLanguage here, attachBaseContext might use this new one if a recreate happens
             // before VM loads from DataStore. This could be fine.
             // currentLanguage = loginViewModel.uiState.value.currentLanguage
        }


        setContent {
            // Observe ViewModel's UI state
            val appState by loginViewModel.uiState.collectAsState()
            Log.d("MainActivity", "setContent: Composing with language ${appState.currentLanguage} (from ViewModel state)")


            // Effect to handle language changes from ViewModel (after DataStore load or user selection)
            // and trigger Activity recreation
            LaunchedEffect(appState.currentLanguage) {
                Log.d("MainActivity", "LaunchedEffect: appState.currentLanguage = ${appState.currentLanguage}, MainActivity.currentLanguage = $currentLanguage")
                if (currentLanguage != appState.currentLanguage) {
                    Log.d("MainActivity", "LaunchedEffect: Language changed. Recreating activity. New lang: ${appState.currentLanguage}")
                    currentLanguage = appState.currentLanguage
                    this@MainActivity.recreate()
                }
            }

            // Theme uses the language state as a key to ensure recomposition on language change
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

// AppNavigation, LoginAppTheme, DefaultPreview as previously defined
// Make sure LoginViewModel is passed correctly.

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
    // For preview, we need a context to create the factory
    val context = LocalContext.current
    val factory = LoginViewModelFactory(context.applicationContext)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    LoginAppTheme {
        AppNavigation(loginViewModel)
    }
}
