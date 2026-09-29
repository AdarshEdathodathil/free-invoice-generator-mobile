package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

import com.example.freeinvoicegeneratorbydaybookcloud.shared.currentTimeMillis

data class InvoiceItem(
    val id: Long = 0L, val invoiceId: Long = 0L, val name: String, val description: String? = null,
    val quantity: Double, val unitPriceMinor: Long, val taxPercent: Double = 0.0,
    val lineSubtotalMinor: Long, val discountPercent: Double = 0.0, val discountMinor: Long = 0L,
    val taxableAmountMinor: Long = lineSubtotalMinor, val cgstPercent: Double = 0.0,
    val cgstAmountMinor: Long = 0L, val sgstPercent: Double = 0.0, val sgstAmountMinor: Long = 0L,
    val igstPercent: Double = 0.0, val igstAmountMinor: Long = 0L, val taxAmountMinor: Long,
    val totalMinor: Long, val createdAt: Long = currentTimeMillis(), val updatedAt: Long = currentTimeMillis()
)
