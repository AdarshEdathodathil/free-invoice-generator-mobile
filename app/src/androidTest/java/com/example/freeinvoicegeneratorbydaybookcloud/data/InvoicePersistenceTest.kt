package com.example.freeinvoicegeneratorbydaybookcloud.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.InvoiceRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvoicePersistenceTest {
    @Test
    fun invoiceRemainsAfterDatabaseIsClosedAndReopened() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "invoice-persistence-${UUID.randomUUID()}.db"

        try {
            var database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
            val organizationId = database.organizationDao().insertOrganization(
                OrganizationEntity(name = "Persistent Company", address = "1 Storage Street")
            )
            val customerId = database.customerDao().insertCustomer(
                CustomerEntity(name = "Saved Customer", address = "2 Customer Road")
            )
            val invoiceId = database.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-TEST",
                    organizationId = organizationId,
                    customerId = customerId,
                    invoiceDate = 1_700_000_000_000L,
                    dueDate = 1_701_000_000_000L,
                    subtotalMinor = 12_500L,
                    taxAmountMinor = 1_250L,
                    totalMinor = 13_750L
                )
            )
            database.invoiceItemDao().insertItem(
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    name = "Persistent service",
                    quantity = 1.0,
                    unitPriceMinor = 12_500L,
                    taxPercent = 10.0,
                    lineSubtotalMinor = 12_500L,
                    taxAmountMinor = 1_250L,
                    totalMinor = 13_750L
                )
            )
            database.close()

            database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
            val restored = database.invoiceDao().getInvoiceWithItemsById(invoiceId)

            assertNotNull(restored)
            assertEquals("INV-TEST", restored?.invoice?.invoiceNumber)
            assertEquals("Persistent Company", restored?.organization?.name)
            assertEquals("Saved Customer", restored?.customer?.name)
            assertEquals("Persistent service", restored?.items?.single()?.name)
            assertEquals(13_750L, restored?.invoice?.totalMinor)
            database.close()
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun invoiceCanBeUpdatedAndDeletedWithItsItems() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "invoice-edit-delete-${UUID.randomUUID()}.db"

        try {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
            val organizationId = database.organizationDao().insertOrganization(
                OrganizationEntity(name = "Edit Company")
            )
            val customerId = database.customerDao().insertCustomer(
                CustomerEntity(name = "Edit Customer")
            )
            val repository = InvoiceRepositoryImpl(
                database,
                database.invoiceDao(),
                database.invoiceItemDao(),
                database.paymentDetailsDao()
            )
            val invoiceId = repository.saveInvoice(
                Invoice(
                    invoiceNumber = "INV-BEFORE",
                    organizationId = organizationId,
                    customerId = customerId,
                    invoiceDate = 1_700_000_000_000L,
                    dueDate = 1_701_000_000_000L,
                    subtotalMinor = 10_000L,
                    taxAmountMinor = 1_000L,
                    totalMinor = 11_000L
                ),
                listOf(
                    InvoiceItem(
                        name = "Old item",
                        quantity = 1.0,
                        unitPriceMinor = 10_000L,
                        taxPercent = 10.0,
                        lineSubtotalMinor = 10_000L,
                        taxAmountMinor = 1_000L,
                        totalMinor = 11_000L
                    )
                )
            )

            val original = requireNotNull(repository.getInvoiceById(invoiceId))
            repository.updateInvoice(
                original.copy(
                    invoiceNumber = "INV-AFTER",
                    subtotalMinor = 20_000L,
                    taxAmountMinor = 2_000L,
                    totalMinor = 22_000L
                ),
                listOf(
                    InvoiceItem(
                        name = "Updated item",
                        quantity = 2.0,
                        unitPriceMinor = 10_000L,
                        taxPercent = 10.0,
                        lineSubtotalMinor = 20_000L,
                        taxAmountMinor = 2_000L,
                        totalMinor = 22_000L
                    )
                )
            )

            val updated = requireNotNull(repository.getInvoiceById(invoiceId))
            assertEquals("INV-AFTER", updated.invoiceNumber)
            assertEquals("Updated item", updated.items.single().name)
            assertEquals(22_000L, updated.totalMinor)

            repository.deleteInvoice(updated)
            assertEquals(null, repository.getInvoiceById(invoiceId))
            assertEquals(emptyList<InvoiceItemEntity>(), database.invoiceItemDao().getItemsForInvoice(invoiceId))
            database.close()
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
