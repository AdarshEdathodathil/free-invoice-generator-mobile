package com.example.freeinvoicegeneratorbydaybookcloud.data.mapper

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.PaymentDetailsEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentDetails

fun PaymentDetailsEntity.toDomain(): PaymentDetails = PaymentDetails(
    id = id,
    invoiceId = invoiceId,
    paymentMethod = paymentMethod,
    accountNumber = accountNumber,
    accountOwnerName = accountOwnerName,
    bankName = bankName,
    upiId = upiId
)

fun PaymentDetails.toEntity(): PaymentDetailsEntity = PaymentDetailsEntity(
    id = id,
    invoiceId = invoiceId,
    paymentMethod = paymentMethod,
    accountNumber = accountNumber,
    accountOwnerName = accountOwnerName,
    bankName = bankName,
    upiId = upiId
)
