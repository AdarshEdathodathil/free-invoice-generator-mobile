package com.example.freeinvoicegeneratorbydaybookcloud.data.repository

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoiceDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.InvoicePersistenceDao
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

class InvoiceRepositoryImpl(
    private val db: AppDatabase
) : InvoiceRepository {
    private val invoiceDao: InvoiceDao = db.invoiceDao()
    private val persistenceDao: InvoicePersistenceDao = db.invoicePersistenceDao()

    override fun observeInvoices(): Flow<List<Invoice>> =
        invoiceDao.observeInvoicesWithItems().map { list -> list.map { it.toDomain() } }

    override fun observeRecentInvoices(limit: Int): Flow<List<Invoice>> =
        invoiceDao.observeRecentInvoicesWithItems(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getInvoiceById(id: Long): Invoice? =
        invoiceDao.getInvoiceWithItemsById(id)?.toDomain()

    override fun observeInvoiceById(id: Long): Flow<Invoice?> =
        invoiceDao.observeInvoiceWithItems(id).map { it?.toDomain() }

    override suspend fun saveInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails?
    ): Long = persistenceDao.saveInvoiceWithItems(
        invoice = invoice.toEntity(),
        items = items.map { it.toEntity() },
        paymentDetails = paymentDetails?.toEntity()
    )

    override suspend fun updateInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails?
    ) {
        persistenceDao.updateInvoiceWithItems(
            invoice = invoice.toEntity(),
            items = items.map { it.toEntity() },
            paymentDetails = paymentDetails?.toEntity()
        )
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
