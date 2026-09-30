package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

data class CountryOption(val name: String, val dialCode: String)

val invoiceCountries = listOf(
    CountryOption("India", "+91"),
    CountryOption("United States", "+1"),
    CountryOption("United Kingdom", "+44"),
    CountryOption("United Arab Emirates", "+971"),
    CountryOption("Saudi Arabia", "+966"),
    CountryOption("Singapore", "+65"),
    CountryOption("Australia", "+61"),
    CountryOption("Canada", "+1"),
    CountryOption("Germany", "+49"),
    CountryOption("France", "+33"),
    CountryOption("Japan", "+81"),
    CountryOption("China", "+86"),
    CountryOption("South Africa", "+27"),
    CountryOption("Brazil", "+55"),
    CountryOption("Mexico", "+52")
)

fun isValidEmail(value: String): Boolean =
    value.isBlank() || Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(value.trim())

fun isValidMobile(value: String): Boolean {
    if (value.isBlank()) return true
    val digits = value.filter(Char::isDigit)
    return digits.length in 8..15
}

fun digitsOnly(value: String, maxLength: Int = 15): String =
    value.filter(Char::isDigit).take(maxLength)

fun decimalInput(value: String): String {
    val cleaned = value.filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    return if (firstDot == -1) cleaned else cleaned.take(firstDot + 1) + cleaned.drop(firstDot + 1).replace(".", "")
}

fun wholeNumberInput(value: String, maxLength: Int = 6): String =
    value.filter(Char::isDigit).take(maxLength)

fun countryForValue(value: String): CountryOption =
    invoiceCountries.firstOrNull { value == it.name || value.startsWith(it.dialCode) }
        ?: invoiceCountries.first()

fun mobileNumberPart(value: String, dialCode: String): String =
    value.removePrefix(dialCode).trim().filter(Char::isDigit)

fun combineMobile(dialCode: String, number: String): String =
    digitsOnly(number).takeIf { it.isNotBlank() }?.let { "$dialCode $it" }.orEmpty()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleChoiceMenu(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}
