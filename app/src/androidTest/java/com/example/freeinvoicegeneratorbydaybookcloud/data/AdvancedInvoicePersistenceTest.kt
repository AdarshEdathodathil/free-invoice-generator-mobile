package com.example.freeinvoicegeneratorbydaybookcloud.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.repository.InvoiceRepositoryImpl
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdvancedInvoicePersistenceTest {
    @Test fun advancedSnapshotPaymentUpdateAndCascadeDelete() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val organizationId = db.organizationDao().insertOrganization(OrganizationEntity(
                name = "GST Business", address = "Kochi", country = "India", mobile = "9999999999",
                gstin = "32AAAAA0000A1Z5", authorityName = "Asha", authorityDesignation = "Director"
            ))
            val customerId = db.customerDao().insertCustomer(CustomerEntity(
                name = "Advanced Customer", address = "Chennai", country = "India",
                mobile = "8888888888", gstin = "33BBBBB0000B1Z5"
            ))
            val repository = InvoiceRepositoryImpl(db, db.invoiceDao(), db.invoiceItemDao(), db.paymentDetailsDao())
            val invoiceId = repository.saveInvoice(
                Invoice(
                    invoiceNumber = "ADV-001", organizationId = organizationId, customerId = customerId,
                    invoiceDate = 1L, dueDate = 2L, subtotalMinor = 10_000L, discountMinor = 1_000L,
                    taxAmountMinor = 1_620L, totalMinor = 10_625L, invoiceType = InvoiceType.ADVANCED,
                    currencyCode = "INR", currencySymbol = "₹", decimalPlaces = 2,
                    deliveryState = "Kerala", taxOption = TaxOption.IGST,
                    showItemDescription = true, showItemDiscount = true, internationalNumbering = true,
                    roundOffMinor = 5L, additionalNotes = "Snapshot note", termsAndConditions = "Net 15"
                ),
                listOf(InvoiceItem(
                    name = "Consulting", description = "Monthly service", quantity = 1.0,
                    unitPriceMinor = 10_000L, lineSubtotalMinor = 10_000L, discountPercent = 10.0,
                    discountMinor = 1_000L, taxableAmountMinor = 9_000L, igstPercent = 18.0,
                    igstAmountMinor = 1_620L, taxPercent = 18.0, taxAmountMinor = 1_620L, totalMinor = 10_620L
                )),
                PaymentDetails(paymentMethod = PaymentMethod.UPI, upiId = "billing@upi")
            )

            val saved = requireNotNull(repository.getInvoiceById(invoiceId))
            assertEquals(InvoiceType.ADVANCED, saved.invoiceType)
            assertEquals("32AAAAA0000A1Z5", saved.organization?.gstin)
            assertEquals("33BBBBB0000B1Z5", saved.customer?.gstin)
            assertEquals("billing@upi", saved.paymentDetails?.upiId)
            assertEquals("Monthly service", saved.items.single().description)

            repository.updateInvoice(saved.copy(totalMinor = 10_630L), saved.items,
                PaymentDetails(paymentMethod = PaymentMethod.BANK, bankName = "Daybook Bank"))
            val updated = requireNotNull(repository.getInvoiceById(invoiceId))
            assertEquals(10_630L, updated.totalMinor)
            assertEquals(PaymentMethod.BANK, updated.paymentDetails?.paymentMethod)
            assertEquals("Daybook Bank", updated.paymentDetails?.bankName)

            repository.deleteInvoice(updated)
            assertNull(repository.getInvoiceById(invoiceId))
            assertNull(db.paymentDetailsDao().getByInvoiceId(invoiceId))
        } finally { db.close() }
    }
}
