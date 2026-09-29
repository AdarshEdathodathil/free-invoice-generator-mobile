package com.example.freeinvoicegeneratorbydaybookcloud.domain.repository

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentDetails
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun observeInvoices(): Flow<List<Invoice>>
    fun observeRecentInvoices(limit: Int): Flow<List<Invoice>>
    suspend fun getInvoiceById(id: Long): Invoice?
    fun observeInvoiceById(id: Long): Flow<Invoice?>
    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>, paymentDetails: PaymentDetails? = null): Long
    suspend fun updateInvoice(invoice: Invoice, items: List<InvoiceItem>, paymentDetails: PaymentDetails? = null)
    suspend fun deleteInvoice(invoice: Invoice)
    fun observeInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>>
    fun observeInvoicesByType(type: InvoiceType): Flow<List<Invoice>>
    fun searchInvoices(query: String): Flow<List<Invoice>>
}
