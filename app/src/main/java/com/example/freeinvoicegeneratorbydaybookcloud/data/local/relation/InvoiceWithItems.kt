package com.example.freeinvoicegeneratorbydaybookcloud.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.PaymentDetailsEntity

/**
 * Room relation model that fetches an invoice together with all its line items
 * in a single @Transaction query.
 *
 * Usage in DAO:
 * ```
 * @Transaction
 * @Query("SELECT * FROM invoices WHERE id = :id")
 * fun observeInvoiceWithItems(id: Long): Flow<InvoiceWithItems?>
 * ```
 */
data class InvoiceWithItems(
    @Embedded
    val invoice: InvoiceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>,

    @Relation(parentColumn = "organizationId", entityColumn = "id")
    val organization: OrganizationEntity,

    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: CustomerEntity,

    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val paymentDetails: PaymentDetailsEntity?
)
