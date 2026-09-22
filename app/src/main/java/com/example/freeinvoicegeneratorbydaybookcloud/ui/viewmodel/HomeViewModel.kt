package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.InvoiceRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.OrganizationRepository
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.BusinessSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    organizationRepository: OrganizationRepository,
    businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {
    val organizationName = combine(
        organizationRepository.observeOrganization(),
        businessSettingsRepository.settings
    ) { organization, businessSettings ->
        organization?.name?.trim()?.takeIf { it.isNotBlank() }
            ?: businessSettings.name.trim()
                .takeIf { it.isNotBlank() }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val organizationLogoPath = businessSettingsRepository.settings
        .map { it.logoPath }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), businessSettingsRepository.settings.value.logoPath)

    val recentInvoices = invoiceRepository.observeRecentInvoices(3)
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalInvoicesCount = invoiceRepository.observeInvoices().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
