package com.example.loginapp.features.countrycode.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.loginapp.R // Import R class
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.features.login.LoginViewModel // Shared ViewModel for simplicity here
import com.example.loginapp.features.login.LoginScreenEvent

// Dummy data for country codes - replace with actual data source later
data class Country(val name: String, val code: String, val dialCode: String)

val dummyCountryList = listOf(
    Country("United States", "US", "+1"),
    Country("Canada", "CA", "+1"),
    Country("United Kingdom", "GB", "+44"),
    Country("Germany", "DE", "+49"),
    Country("France", "FR", "+33"),
    Country("Japan", "JP", "+81"),
    Country("South Korea", "KR", "+82"),
    Country("Taiwan", "TW", "+886"),
    Country("China", "CN", "+86"),
    Country("India", "IN", "+91"),
    Country("Brazil", "BR", "+55"),
    Country("Australia", "AU", "+61")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel() // Accessing LoginViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery, dummyCountryList) {
        if (searchQuery.isBlank()) {
            dummyCountryList
        } else {
            dummyCountryList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.dialCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.select_country_code_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(id = R.string.search_country_label)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.search_icon_desc)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCountries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.no_countries_found))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCountries, key = { it.code + it.dialCode }) { country ->
                        CountryCodeItem(country = country) { selectedCountry ->
                            // Send event to LoginViewModel to update the selected country code
                            loginViewModel.sendEvent(LoginScreenEvent.CountryCodeSelected(selectedCountry.dialCode))
                            navController.popBackStack() // Navigate back to LoginScreen
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun CountryCodeItem(
    country: Country,
    onCountrySelected: (Country) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCountrySelected(country) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // In a real app, you might have a flag icon here
        // For now, just text
        Text(
            text = "${country.name} (${country.dialCode})",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CountryCodeScreenPreview() {
    MaterialTheme {
        CountryCodeScreen(navController = rememberNavController(), loginViewModel = viewModel())
    }
}

@Preview(showBackground = true, name = "CountryCodeScreen Empty Search")
@Composable
fun CountryCodeScreenEmptySearchPreview() {
    // This preview won't reflect runtime search filtering accurately without state manipulation here.
    // For a more accurate preview of empty state, you'd mock the filteredCountries to be empty.
    MaterialTheme {
         val loginViewModel: LoginViewModel = viewModel()
        // Simulate empty search result for preview
        // To effectively preview the "No countries found" state, you'd ideally control
        // the `searchQuery` state from within this preview, or pass a mocked ViewModel
        // whose state reflects this scenario. The current setup relies on internal `remember`
        // state for `searchQuery`.
        // For a simple visual check, you could pass an empty list directly if the Composable allowed it,
        // but `filteredCountries` is derived internally.
        // The most straightforward way to see "No countries found" is to run in an emulator and type
        // a non-matching query.
         CountryCodeScreen(navController = rememberNavController(), loginViewModel = loginViewModel)
    }
}
