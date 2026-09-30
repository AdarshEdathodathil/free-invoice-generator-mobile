package com.example.freeinvoicegeneratorbydaybookcloud.data.preferences

import com.example.freeinvoicegeneratorbydaybookcloud.platform.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
) {
    val displayName: String get() = "$code ($symbol) - $name"
}

val majorCurrencies = listOf(
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound"),
    CurrencyOption("INR", "₹", "Indian Rupee"),
    CurrencyOption("JPY", "¥", "Japanese Yen"),
    CurrencyOption("CNY", "¥", "Chinese Yuan"),
    CurrencyOption("AUD", "A$", "Australian Dollar"),
    CurrencyOption("CAD", "C$", "Canadian Dollar"),
    CurrencyOption("CHF", "CHF", "Swiss Franc"),
    CurrencyOption("SGD", "S$", "Singapore Dollar"),
    CurrencyOption("AED", "د.إ", "UAE Dirham"),
    CurrencyOption("SAR", "﷼", "Saudi Riyal"),
    CurrencyOption("NZD", "NZ$", "New Zealand Dollar"),
    CurrencyOption("HKD", "HK$", "Hong Kong Dollar"),
    CurrencyOption("KRW", "₩", "South Korean Won"),
    CurrencyOption("ZAR", "R", "South African Rand"),
    CurrencyOption("BRL", "R$", "Brazilian Real"),
    CurrencyOption("MXN", "MX$", "Mexican Peso")
)

data class InvoiceSettings(
    val prefix: String = "INV-",
    val taxRatePercent: Int = 10,
    val currencyCode: String = "USD",
    val templateId: String = "modern_teal"
) {
    val currency: CurrencyOption
        get() = majorCurrencies.firstOrNull { it.code == currencyCode } ?: majorCurrencies.first()
}

class InvoiceSettingsRepository(
    private val store: KeyValueStore
) {
    private val _settings = MutableStateFlow(
        InvoiceSettings(
            prefix = store.getString("invoice_prefix", "INV-"),
            taxRatePercent = store.getInt("invoice_tax_rate", 10).coerceIn(0, 100),
            currencyCode = store.getString("invoice_currency", "USD"),
            templateId = store.getString("invoice_template", "modern_teal")
        )
    )
    val settings: StateFlow<InvoiceSettings> = _settings.asStateFlow()

    fun update(prefix: String, taxRatePercent: Int, currencyCode: String) {
        val normalized = InvoiceSettings(
            prefix = prefix.trim().ifBlank { "INV-" },
            taxRatePercent = taxRatePercent.coerceIn(0, 100),
            currencyCode = majorCurrencies.firstOrNull { it.code == currencyCode }?.code ?: "USD",
            templateId = _settings.value.templateId
        )
        store.putString("invoice_prefix", normalized.prefix)
        store.putInt("invoice_tax_rate", normalized.taxRatePercent)
        store.putString("invoice_currency", normalized.currencyCode)
        _settings.value = normalized
    }

    fun updateTemplate(templateId: String) {
        val normalized = _settings.value.copy(templateId = templateId)
        store.putString("invoice_template", normalized.templateId)
        _settings.value = normalized
    }
}
