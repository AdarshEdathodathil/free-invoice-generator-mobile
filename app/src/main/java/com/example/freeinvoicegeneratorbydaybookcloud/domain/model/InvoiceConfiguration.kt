package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

enum class InvoiceType { SIMPLE, ADVANCED }

enum class TaxOption { CGST_SGST, IGST, NON_TAXABLE }

enum class DateFormatOption(val pattern: String, val label: String) {
    DD_MM_YYYY("dd-MM-yyyy", "DD-MM-YYYY"),
    MM_DD_YYYY("MM-dd-yyyy", "MM-DD-YYYY"),
    YYYY_MM_DD("yyyy-MM-dd", "YYYY-MM-DD")
}

enum class PaymentMethod { NONE, BANK, UPI }

