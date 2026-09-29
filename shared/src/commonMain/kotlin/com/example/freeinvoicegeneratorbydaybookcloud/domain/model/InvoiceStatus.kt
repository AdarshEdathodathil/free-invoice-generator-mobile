package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

enum class InvoiceStatus {
    DRAFT, SENT, PAID, OVERDUE;
    companion object {
        fun fromString(value: String): InvoiceStatus = entries.firstOrNull { it.name.equals(value, true) } ?: DRAFT
    }
}
