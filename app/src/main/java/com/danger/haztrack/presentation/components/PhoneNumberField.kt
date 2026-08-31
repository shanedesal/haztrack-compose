package com.danger.haztrack.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.danger.haztrack.R
import com.danger.haztrack.util.CountryInfo

private val CountrySelectorHeight = 56.dp
private val CountrySelectorMinWidth = 96.dp
private val BottomSheetMaxListHeight = 420.dp

/**
 * A country-code selector chip (flag + dial code) next to a national-number text field.
 * Selecting a country opens a searchable bottom sheet built from [countries] (see
 * [com.danger.haztrack.util.CountryCodeProvider]) — no third-party UI dependency.
 */
@Composable
fun PhoneNumberField(
    countries: List<CountryInfo>,
    selectedCountry: CountryInfo?,
    phoneNumber: String,
    onCountrySelected: (CountryInfo) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    var isPickerVisible by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        CountrySelectorChip(
            country = selectedCountry,
            enabled = enabled,
            onClick = { isPickerVisible = true },
        )
        Spacer(modifier = Modifier.width(12.dp))
        HaztrackTextField(
            value = phoneNumber,
            onValueChange = { input -> onPhoneNumberChange(input.filter(Char::isDigit)) },
            label = label,
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            supportingText = supportingText,
            isError = isError,
            enabled = enabled,
        )
    }

    if (isPickerVisible) {
        CountryPickerBottomSheet(
            countries = countries,
            onDismiss = { isPickerVisible = false },
            onCountrySelected = { country ->
                onCountrySelected(country)
                isPickerVisible = false
            },
        )
    }
}

@Composable
private fun CountrySelectorChip(
    country: CountryInfo?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(CountrySelectorHeight)
            .width(CountrySelectorMinWidth)
            .clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = country?.flagEmoji.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = country?.dialCode.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.phone_field_country_selector_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPickerBottomSheet(
    countries: List<CountryInfo>,
    onDismiss: () -> Unit,
    onCountrySelected: (CountryInfo) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filteredCountries = remember(countries, query) {
        if (query.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.displayName.contains(query, ignoreCase = true) ||
                    country.dialCode.contains(query)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        HaztrackTextField(
            value = query,
            onValueChange = { query = it },
            label = stringResource(R.string.phone_field_search_label),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            leadingIcon = Icons.Filled.Search,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.height(BottomSheetMaxListHeight)) {
            items(filteredCountries, key = { it.regionCode }) { country ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCountrySelected(country) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = country.flagEmoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = country.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
