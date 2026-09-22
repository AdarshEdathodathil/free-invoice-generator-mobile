package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

data class PaymentDetails(
    val id: Long = 0L,
    val invoiceId: Long = 0L,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val accountNumber: String? = null,
    val accountOwnerName: String? = null,
    val bankName: String? = null,
    val upiId: String? = null
)
