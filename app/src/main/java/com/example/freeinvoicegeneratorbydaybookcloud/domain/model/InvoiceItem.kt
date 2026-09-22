package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

/**
 * Domain model representing a single line item within an invoice.
 * Framework-independent — no Room or Android annotations.
 *
 * Monetary values are stored in the smallest currency unit (e.g. paise for INR, cents for USD).
 */
data class InvoiceItem(
    val id: Long = 0L,
    val invoiceId: Long = 0L,
    val name: String,
    val description: String? = null,
    /** Fractional quantities are supported (e.g. 1.5 hours). */
    val quantity: Double,
    /** Unit price in smallest currency unit (e.g. paise). */
    val unitPriceMinor: Long,
    /** Tax percentage (e.g. 18.0 for 18% GST). */
    val taxPercent: Double = 0.0,
    /** Subtotal before tax: quantity × unitPriceMinor */
    val lineSubtotalMinor: Long,
    val discountPercent: Double = 0.0,
    val discountMinor: Long = 0L,
    val taxableAmountMinor: Long = lineSubtotalMinor,
    val cgstPercent: Double = 0.0,
    val cgstAmountMinor: Long = 0L,
    val sgstPercent: Double = 0.0,
    val sgstAmountMinor: Long = 0L,
    val igstPercent: Double = 0.0,
    val igstAmountMinor: Long = 0L,
    /** Tax amount on this line item. */
    val taxAmountMinor: Long,
    /** Total including tax: lineSubtotalMinor + taxAmountMinor */
    val totalMinor: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
