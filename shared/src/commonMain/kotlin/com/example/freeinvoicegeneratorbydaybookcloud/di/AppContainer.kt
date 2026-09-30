package com.example.freeinvoicegeneratorbydaybookcloud.di

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.createAppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.BusinessSettingsRepository
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.InvoiceSettingsRepository
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.CustomerRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.InvoiceRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.OrganizationRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.CustomerRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.InvoiceRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.OrganizationRepository
import com.example.freeinvoicegeneratorbydaybookcloud.platform.KeyValueStore
import com.example.freeinvoicegeneratorbydaybookcloud.platform.PlatformActions
import com.example.freeinvoicegeneratorbydaybookcloud.platform.configurePlatformActions
import com.example.freeinvoicegeneratorbydaybookcloud.platform.createKeyValueStore
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.HomeViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoicesViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.SettingsViewModel

class AppContainer(
    val keyValueStore: KeyValueStore = createKeyValueStore(),
    val platformActions: PlatformActions = configurePlatformActions()
) {
    private val database = createAppDatabase()

    val invoiceRepository: InvoiceRepository = InvoiceRepositoryImpl(database)
    val customerRepository: CustomerRepository = CustomerRepositoryImpl(database.customerDao())
    val organizationRepository: OrganizationRepository = OrganizationRepositoryImpl(database.organizationDao())
    val businessSettingsRepository = BusinessSettingsRepository(keyValueStore)
    val invoiceSettingsRepository = InvoiceSettingsRepository(keyValueStore)

    fun homeViewModel() = HomeViewModel(
        invoiceRepository = invoiceRepository,
        organizationRepository = organizationRepository,
        businessSettingsRepository = businessSettingsRepository
    )

    fun invoicesViewModel() = InvoicesViewModel(invoiceRepository)

    fun settingsViewModel() = SettingsViewModel(
        keyValueStore = keyValueStore,
        invoiceSettingsRepository = invoiceSettingsRepository,
        businessSettingsRepository = businessSettingsRepository
    )

    fun createInvoiceViewModel() = CreateInvoiceViewModel(
        invoiceRepository = invoiceRepository,
        customerRepository = customerRepository,
        organizationRepository = organizationRepository,
        invoiceSettingsRepository = invoiceSettingsRepository,
        businessSettingsRepository = businessSettingsRepository,
        platformActions = platformActions
    )
}
