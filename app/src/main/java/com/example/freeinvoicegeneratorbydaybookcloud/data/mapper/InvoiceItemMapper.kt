package com.example.freeinvoicegeneratorbydaybookcloud.data.mapper

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem

/** Maps an [InvoiceItemEntity] (Room) to its [InvoiceItem] domain model. */
fun InvoiceItemEntity.toDomain(): InvoiceItem = InvoiceItem(
    id = id,
    invoiceId = invoiceId,
    name = name,
    description = description,
    quantity = quantity,
    unitPriceMinor = unitPriceMinor,
    taxPercent = taxPercent,
    lineSubtotalMinor = lineSubtotalMinor,
    discountPercent = discountPercent,
    discountMinor = discountMinor,
    taxableAmountMinor = taxableAmountMinor,
    cgstPercent = cgstPercent,
    cgstAmountMinor = cgstAmountMinor,
    sgstPercent = sgstPercent,
    sgstAmountMinor = sgstAmountMinor,
    igstPercent = igstPercent,
    igstAmountMinor = igstAmountMinor,
    taxAmountMinor = taxAmountMinor,
    totalMinor = totalMinor,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/** Maps an [InvoiceItem] domain model to its [InvoiceItemEntity] (Room). */
fun InvoiceItem.toEntity(): InvoiceItemEntity = InvoiceItemEntity(
    id = id,
    invoiceId = invoiceId,
    name = name,
    description = description,
    quantity = quantity,
    unitPriceMinor = unitPriceMinor,
    taxPercent = taxPercent,
    lineSubtotalMinor = lineSubtotalMinor,
    discountPercent = discountPercent,
    discountMinor = discountMinor,
    taxableAmountMinor = taxableAmountMinor,
    cgstPercent = cgstPercent,
    cgstAmountMinor = cgstAmountMinor,
    sgstPercent = sgstPercent,
    sgstAmountMinor = sgstAmountMinor,
    igstPercent = igstPercent,
    igstAmountMinor = igstAmountMinor,
    taxAmountMinor = taxAmountMinor,
    totalMinor = totalMinor,
    createdAt = createdAt,
    updatedAt = updatedAt
)
