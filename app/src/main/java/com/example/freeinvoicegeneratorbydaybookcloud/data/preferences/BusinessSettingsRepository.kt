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
    val address: String = "123 Business Street, City, State",
    val email: String = "hello@daybook.cloud",
    val phone: String = "+1 555-0192"
)

@Singleton
class BusinessSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("daybook_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        BusinessSettings(
            name = preferences.getString("business_name", DEFAULT_NAME) ?: DEFAULT_NAME,
            address = preferences.getString("business_address", "123 Business Street, City, State")
                ?: "123 Business Street, City, State",
            email = preferences.getString("business_email", "hello@daybook.cloud") ?: "hello@daybook.cloud",
            phone = preferences.getString("business_phone", "+1 555-0192") ?: "+1 555-0192"
        )
    )
    val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    fun update(name: String, address: String, email: String, phone: String) {
        val normalized = BusinessSettings(
            name = name.trim(),
            address = address.trim(),
            email = email.trim(),
            phone = phone.trim()
        )
        preferences.edit()
            .putString("business_name", normalized.name)
            .putString("business_address", normalized.address)
            .putString("business_email", normalized.email)
            .putString("business_phone", normalized.phone)
            .apply()
        _settings.value = normalized
    }

    companion object {
        const val DEFAULT_NAME = "Your Company"
    }
}
