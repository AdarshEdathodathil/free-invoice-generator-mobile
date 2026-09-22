package com.example.freeinvoicegeneratorbydaybookcloud.di

import android.content.Context
import androidx.room.Room
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.CustomerDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceItemDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.OrganizationDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.PaymentDetailsDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database and all DAOs.
 *
 * Installed in [SingletonComponent] so the database is created once per application lifecycle.
 * The database file is named "daybook_cloud.db".
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "daybook_cloud.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideOrganizationDao(db: AppDatabase): OrganizationDao = db.organizationDao()

    @Provides
    @Singleton
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    @Singleton
    fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()

    @Provides
    @Singleton
    fun provideInvoiceItemDao(db: AppDatabase): InvoiceItemDao = db.invoiceItemDao()

    @Provides
    @Singleton
    fun providePaymentDetailsDao(db: AppDatabase): PaymentDetailsDao = db.paymentDetailsDao()
}
