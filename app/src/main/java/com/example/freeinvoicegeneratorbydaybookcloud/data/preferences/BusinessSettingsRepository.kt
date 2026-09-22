package com.example.freeinvoicegeneratorbydaybookcloud.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BusinessSettings(
    val name: String = BusinessSettingsRepository.DEFAULT_NAME,
    val address: String = BusinessSettingsRepository.DEFAULT_ADDRESS,
    val email: String = "",
    val phone: String = "",
    val logoPath: String? = null
)

@Singleton
class BusinessSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("daybook_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        BusinessSettings(
            name = normalizeDefaultText(preferences.getString("business_name", DEFAULT_NAME) ?: DEFAULT_NAME),
            address = normalizeDefaultText(preferences.getString("business_address", DEFAULT_ADDRESS) ?: DEFAULT_ADDRESS),
            email = preferences.getString("business_email", "") ?: "",
            phone = preferences.getString("business_phone", "") ?: "",
            logoPath = preferences.getString("business_logo_path", null)
        )
    )
    val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    fun update(
        name: String,
        address: String,
        email: String,
        phone: String,
        logoPath: String? = _settings.value.logoPath
    ) {
        val normalized = BusinessSettings(
            name = name.trim(),
            address = address.trim(),
            email = email.trim(),
            phone = phone.trim(),
            logoPath = logoPath
        )
        preferences.edit()
            .putString("business_name", normalized.name)
            .putString("business_address", normalized.address)
            .putString("business_email", normalized.email)
            .putString("business_phone", normalized.phone)
            .putString("business_logo_path", normalized.logoPath)
            .apply()
        _settings.value = normalized
    }

    companion object {
        const val DEFAULT_NAME = ""
        const val DEFAULT_ADDRESS = ""
        private const val OLD_DEFAULT_NAME = "Your Company"
        private const val OLD_DEFAULT_ADDRESS = "123 Business Street, City, State"

        private fun normalizeDefaultText(value: String): String =
            value.takeUnless { it == OLD_DEFAULT_NAME || it == OLD_DEFAULT_ADDRESS }.orEmpty()
    }
}
