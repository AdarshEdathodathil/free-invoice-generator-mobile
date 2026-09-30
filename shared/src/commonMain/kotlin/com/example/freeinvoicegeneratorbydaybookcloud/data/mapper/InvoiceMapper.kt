package com.example.freeinvoicegeneratorbydaybookcloud.data.mapper

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.relation.InvoiceWithItems
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus

/** Maps an [InvoiceEntity] (Room) to its [Invoice] domain model. */
fun InvoiceEntity.toDomain(items: List<com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem> = emptyList()): Invoice =
    Invoice(
        id = id,
        invoiceNumber = invoiceNumber,
        organizationId = organizationId,
        customerId = customerId,
        invoiceDate = invoiceDate,
        dueDate = dueDate,
        subtotalMinor = subtotalMinor,
        taxAmountMinor = taxAmountMinor,
        discountMinor = discountMinor,
        totalMinor = totalMinor,
        status = InvoiceStatus.fromString(status),
        notes = notes,
        invoiceType = invoiceType,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        decimalPlaces = decimalPlaces,
        deliveryState = deliveryState,
        taxOption = taxOption,
        dateFormat = dateFormat,
        showItemDescription = showItemDescription,
        showItemDiscount = showItemDiscount,
        internationalNumbering = internationalNumbering,
        roundOffMinor = roundOffMinor,
        additionalNotes = additionalNotes,
        termsAndConditions = termsAndConditions,
        createdAt = createdAt,
        updatedAt = updatedAt,
        items = items
    )

/** Maps an [Invoice] domain model to its [InvoiceEntity] (Room). Items are stored separately. */
fun Invoice.toEntity(): InvoiceEntity = InvoiceEntity(
    id = id,
    invoiceNumber = invoiceNumber,
    organizationId = organizationId,
    customerId = customerId,
    invoiceDate = invoiceDate,
    dueDate = dueDate,
    subtotalMinor = subtotalMinor,
    taxAmountMinor = taxAmountMinor,
    discountMinor = discountMinor,
    totalMinor = totalMinor,
    status = status.name,
    notes = notes,
    invoiceType = invoiceType,
    currencyCode = currencyCode,
    currencySymbol = currencySymbol,
    decimalPlaces = decimalPlaces,
    deliveryState = deliveryState,
    taxOption = taxOption,
    dateFormat = dateFormat,
    showItemDescription = showItemDescription,
    showItemDiscount = showItemDiscount,
    internationalNumbering = internationalNumbering,
    roundOffMinor = roundOffMinor,
    additionalNotes = additionalNotes,
    termsAndConditions = termsAndConditions,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InvoiceWithItems.toDomain(): Invoice = invoice.toDomain(
    items = items.map { it.toDomain() }
).copy(
    organization = organization.toDomain(),
    customer = customer.toDomain(),
    paymentDetails = paymentDetails?.toDomain()
)
