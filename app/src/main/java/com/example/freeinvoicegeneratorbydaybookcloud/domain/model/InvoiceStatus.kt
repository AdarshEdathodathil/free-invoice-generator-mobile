package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

/**
 * Represents the lifecycle state of an invoice.
 * Stored in the database as a String via [InvoiceStatusConverter].
 */
enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE;

    companion object {
        fun fromString(value: String): InvoiceStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
    }
}

