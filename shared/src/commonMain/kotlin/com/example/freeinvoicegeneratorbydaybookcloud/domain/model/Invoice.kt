package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

import com.example.freeinvoicegeneratorbydaybookcloud.shared.currentTimeMillis

data class Invoice(
    val id: Long = 0L, val invoiceNumber: String, val organizationId: Long, val customerId: Long,
    val invoiceDate: Long, val dueDate: Long, val subtotalMinor: Long, val taxAmountMinor: Long,
    val discountMinor: Long = 0L, val totalMinor: Long, val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val notes: String? = null, val invoiceType: InvoiceType = InvoiceType.SIMPLE, val currencyCode: String = "USD",
    val currencySymbol: String = "$", val decimalPlaces: Int = 2, val deliveryState: String? = null,
    val taxOption: TaxOption = TaxOption.NON_TAXABLE, val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val showItemDescription: Boolean = false, val showItemDiscount: Boolean = false,
    val internationalNumbering: Boolean = false, val roundOffMinor: Long = 0L,
    val additionalNotes: String? = null, val termsAndConditions: String? = null,
    val createdAt: Long = currentTimeMillis(), val updatedAt: Long = currentTimeMillis(),
    val items: List<InvoiceItem> = emptyList(), val organization: Organization? = null,
    val customer: Customer? = null, val paymentDetails: PaymentDetails? = null
)
