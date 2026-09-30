package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.PaymentDetailsEntity

/**
 * Cross-table invoice writes wrapped in Room [@Transaction] blocks so they compile on KMP
 * without relying on [androidx.room.withTransaction].
 */
@Dao
abstract class InvoicePersistenceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    abstract suspend fun updateInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPaymentDetails(details: PaymentDetailsEntity): Long

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    abstract suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Query("DELETE FROM payment_details WHERE invoiceId = :invoiceId")
    abstract suspend fun deletePaymentForInvoice(invoiceId: Long)

    @Transaction
    open suspend fun saveInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        paymentDetails: PaymentDetailsEntity?
    ): Long {
        val invoiceId = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = invoiceId) })
        paymentDetails?.let { insertPaymentDetails(it.copy(invoiceId = invoiceId)) }
        return invoiceId
    }

    @Transaction
    open suspend fun updateInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        paymentDetails: PaymentDetailsEntity?
    ) {
        updateInvoice(invoice)
        deleteItemsForInvoice(invoice.id)
        insertItems(items.map { it.copy(invoiceId = invoice.id) })
        deletePaymentForInvoice(invoice.id)
        paymentDetails?.let { insertPaymentDetails(it.copy(id = 0L, invoiceId = invoice.id)) }
    }
}
