package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

/**
 * Domain model representing an invoice.
 * Framework-independent — no Room or Android annotations.
 *
 * Monetary values are stored in the smallest currency unit (e.g. paise for INR, cents for USD).
 */
data class Invoice(
    val id: Long = 0L,
    val invoiceNumber: String,
    val organizationId: Long,
    val customerId: Long,
    /** Invoice date as epoch milliseconds. */
    val invoiceDate: Long,
    /** Due date as epoch milliseconds. */
    val dueDate: Long,
    /** Subtotal before tax, in smallest currency unit. */
    val subtotalMinor: Long,
    /** Total tax amount, in smallest currency unit. */
    val taxAmountMinor: Long,
    /** Total discount applied, in smallest currency unit. */
    val discountMinor: Long = 0L,
    /** Final total (subtotal + tax − discount), in smallest currency unit. */
    val totalMinor: Long,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val notes: String? = null,
    val invoiceType: InvoiceType = InvoiceType.SIMPLE,
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val decimalPlaces: Int = 2,
    val deliveryState: String? = null,
    val taxOption: TaxOption = TaxOption.NON_TAXABLE,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val showItemDescription: Boolean = false,
    val showItemDiscount: Boolean = false,
    val internationalNumbering: Boolean = false,
    val roundOffMinor: Long = 0L,
    val additionalNotes: String? = null,
    val termsAndConditions: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    /** Eagerly loaded items — populated by repositories when needed. */
    val items: List<InvoiceItem> = emptyList(),
    val organization: Organization? = null,
    val customer: Customer? = null,
    val paymentDetails: PaymentDetails? = null
)
