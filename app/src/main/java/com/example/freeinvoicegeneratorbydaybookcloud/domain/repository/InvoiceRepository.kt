package com.example.freeinvoicegeneratorbydaybookcloud.domain.repository

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentDetails
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for invoice data.
 * Implementations live in the data layer; this interface has no knowledge of Room.
 *
 * Invoice creation and updates are transactional — the invoice and its items
 * are always persisted atomically.
 */
interface InvoiceRepository {

    /** Observe all invoices, newest first. */
    fun observeInvoices(): Flow<List<Invoice>>

    /** Observe the most recent invoices, newest first. */
    fun observeRecentInvoices(limit: Int): Flow<List<Invoice>>

    /** Get a single invoice by ID, or null if not found. */
    suspend fun getInvoiceById(id: Long): Invoice?

    /** Observe a single invoice reactively, or emit null if not found. */
    fun observeInvoiceById(id: Long): Flow<Invoice?>

    /**
     * Atomically save an invoice together with its line items.
     * The invoice ID is assigned by Room; all items receive this ID before insertion.
     * @return the row ID of the newly inserted invoice.
     */
    suspend fun saveInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails? = null
    ): Long

    /**
     * Atomically update an invoice and replace all its associated items.
     * Old items for this invoice are deleted and replaced with [items].
     */
    suspend fun updateInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        paymentDetails: PaymentDetails? = null
    )

    /** Delete an invoice (associated items are cascade-deleted by Room). */
    suspend fun deleteInvoice(invoice: Invoice)

    /** Observe invoices filtered by a specific status. */
    fun observeInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>>

    fun observeInvoicesByType(type: InvoiceType): Flow<List<Invoice>>

    /**
     * Search invoices by invoice number.
     * @param query LIKE pattern (e.g. "INV-00").
     */
    fun searchInvoices(query: String): Flow<List<Invoice>>
}
