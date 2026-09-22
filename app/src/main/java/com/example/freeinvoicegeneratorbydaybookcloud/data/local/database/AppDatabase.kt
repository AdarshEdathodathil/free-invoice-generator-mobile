package com.example.freeinvoicegeneratorbydaybookcloud.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.converter.InvoiceStatusConverter
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.converter.InvoiceConfigurationConverters
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.CustomerDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceItemDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.OrganizationDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.PaymentDetailsDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.PaymentDetailsEntity

/**
 * Room database for the Daybook.Cloud invoice application.
 *
 * Database file: daybook_cloud.db
 * Version: 1
 *
 * Tables:
 *  - organizations  (OrganizationEntity)
 *  - customers      (CustomerEntity)
 *  - invoices       (InvoiceEntity)
 *  - invoice_items  (InvoiceItemEntity)
 *
 * Schema is exported to app/schemas/ via the KSP room.schemaLocation argument
 * configured in app/build.gradle.kts. Keep schema files in version control.
 *
 * Migration strategy:
 *  - Version 1 is the initial schema — no migrations needed yet.
 *  - Do NOT call fallbackToDestructiveMigration() in production; add
 *    explicit Migration objects when bumping the version number.
 *
 * Singleton lifecycle is managed by Hilt (@Singleton in DatabaseModule).
 * Do NOT instantiate this class manually.
 */
@Database(
    entities = [
        OrganizationEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentDetailsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(InvoiceStatusConverter::class, InvoiceConfigurationConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun organizationDao(): OrganizationDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun paymentDetailsDao(): PaymentDetailsDao
}
