package com.example.freeinvoicegeneratorbydaybookcloud.data.preferences

import com.example.freeinvoicegeneratorbydaybookcloud.platform.KeyValueStore
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

class BusinessSettingsRepository(
    private val store: KeyValueStore
) {
    private val _settings = MutableStateFlow(
        BusinessSettings(
            name = normalizeDefaultText(store.getString("business_name", DEFAULT_NAME)),
            address = normalizeDefaultText(store.getString("business_address", DEFAULT_ADDRESS)),
            email = store.getString("business_email", ""),
            phone = store.getString("business_phone", ""),
            logoPath = store.getStringOrNull("business_logo_path")
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
        store.putString("business_name", normalized.name)
        store.putString("business_address", normalized.address)
        store.putString("business_email", normalized.email)
        store.putString("business_phone", normalized.phone)
        store.putStringOrNull("business_logo_path", normalized.logoPath)
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
