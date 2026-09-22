package com.example.freeinvoicegeneratorbydaybookcloud.data.repository

import androidx.room.withTransaction
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceItemDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.PaymentDetailsDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toDomain
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentDetails
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [InvoiceRepository].
 *
 * Invoice + item saves are wrapped in [AppDatabase.withTransaction] to guarantee
 * atomicity — either both the invoice and all its items are committed, or nothing is.
 */
class InvoiceRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val invoiceDao: InvoiceDao,
    private val itemDao: InvoiceItemDao,
    private val paymentDetailsDao: PaymentDetailsDao
) : InvoiceRepository {

    override fun observeInvoices(): Flow<List<Invoice>> =
        invoiceDao.observeInvoicesWithItems().map { list -> list.map { it.toDomain() } }

    override fun observeRecentInvoices(limit: Int): Flow<List<Invoice>> =
        invoiceDao.observeRecentInvoicesWithItems(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getInvoiceById(id: Long): Invoice? =
        invoiceDao.getInvoiceWithItemsById(id)?.toDomain()

    override fun observeInvoiceById(id: Long): Flow<Invoice?> =
        invoiceDao.observeInvoiceWithItems(id).map { it?.toDomain() }

    /**
     * Atomically saves [invoice] and all [items].
     *
     * Flow:
     *  1. Insert the invoice → get the generated invoiceId
     *  2. Copy invoiceId onto each item entity
     *  3. Insert all items
     *
     * If any step throws, the entire transaction is rolled back.
     */
    override suspend fun saveInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails?
    ): Long =
        db.withTransaction {
            val invoiceId = invoiceDao.insertInvoice(invoice.toEntity())
            val itemEntities = items.map { it.toEntity().copy(invoiceId = invoiceId) }
            itemDao.insertAll(itemEntities)
            paymentDetails?.let { paymentDetailsDao.insert(it.toEntity().copy(invoiceId = invoiceId)) }
            invoiceId
        }

    /**
     * Atomically updates [invoice] and replaces all its associated items with [items].
     *
     * Flow:
     *  1. Update the invoice record
     *  2. Delete all existing items for this invoice
     *  3. Re-insert the new/updated items
     *
     * Uses CASCADE-aware delete so no orphan items remain.
     */
    override suspend fun updateInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails?
    ): Unit =
        db.withTransaction {
            invoiceDao.updateInvoice(invoice.toEntity())
            itemDao.deleteItemsForInvoice(invoice.id)
            val itemEntities = items.map { it.toEntity().copy(invoiceId = invoice.id) }
            itemDao.insertAll(itemEntities)
            paymentDetailsDao.deleteByInvoiceId(invoice.id)
            paymentDetails?.let { paymentDetailsDao.insert(it.toEntity().copy(id = 0L, invoiceId = invoice.id)) }
            Unit
        }

    override suspend fun deleteInvoice(invoice: Invoice) =
        invoiceDao.deleteInvoice(invoice.toEntity())

    override fun observeInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>> =
        observeInvoices().map { list -> list.filter { it.status == status } }

    override fun observeInvoicesByType(type: InvoiceType): Flow<List<Invoice>> =
        observeInvoices().map { list -> list.filter { it.invoiceType == type } }

    override fun searchInvoices(query: String): Flow<List<Invoice>> {
        return observeInvoices().map { list ->
            list.filter { it.invoiceNumber.contains(query, ignoreCase = true) }
        }
    }
}
