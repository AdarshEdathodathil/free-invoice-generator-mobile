package com.example.freeinvoicegeneratorbydaybookcloud.di

import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.CustomerRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.InvoiceRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.OrganizationRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.CustomerRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.InvoiceRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.OrganizationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository implementations to their domain interfaces.
 *
 * Using @Binds (abstract module) is more efficient than @Provides because Dagger
 * can use the implementation directly without a factory method.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOrganizationRepository(
        impl: OrganizationRepositoryImpl
    ): OrganizationRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: InvoiceRepositoryImpl
    ): InvoiceRepository
}

