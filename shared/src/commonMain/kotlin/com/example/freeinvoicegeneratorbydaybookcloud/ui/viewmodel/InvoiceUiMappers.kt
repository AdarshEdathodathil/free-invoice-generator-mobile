package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import kotlin.math.roundToInt

internal fun InvoiceItemUiModel.toDomainInput(): InvoiceItem = InvoiceItem(
    id = id.toLongOrNull() ?: 0L,
    name = name,
    description = description.ifBlank { null },
    quantity = quantity.toDouble(),
    unitPriceMinor = unitPriceMinor,
    discountPercent = discountPercent,
    cgstPercent = cgstPercent,
    sgstPercent = sgstPercent,
    igstPercent = igstPercent,
    lineSubtotalMinor = 0L,
    taxAmountMinor = 0L,
    totalMinor = 0L
)

internal fun Invoice.toUiModel(): InvoiceUiModel = InvoiceUiModel(
    id = id,
    invoiceNumber = invoiceNumber,
    invoiceType = invoiceType,
    organizationName = organization?.name.orEmpty(),
    organizationAddress = organization?.address.orEmpty(),
    organizationCountry = organization?.country.orEmpty(),
    organizationEmail = organization?.email.orEmpty(),
    organizationMobile = organization?.mobile ?: organization?.phone.orEmpty(),
    organizationGstin = organization?.gstin ?: organization?.taxNumber.orEmpty(),
    authorityName = organization?.authorityName.orEmpty(),
    authorityDesignation = organization?.authorityDesignation.orEmpty(),
    organizationLogoPath = organization?.logoPath,
    customerName = customer?.name.orEmpty(),
    customerAddress = customer?.address.orEmpty(),
    customerCountry = customer?.country.orEmpty(),
    customerMobile = customer?.mobile ?: customer?.phone.orEmpty(),
    customerEmail = customer?.email.orEmpty(),
    customerGstin = customer?.gstin ?: customer?.taxNumber.orEmpty(),
    date = formatStoredDate(invoiceDate, dateFormat),
    dueDate = formatStoredDate(dueDate, dateFormat),
    amountMinor = totalMinor,
    status = status.name.lowercase().replaceFirstChar { it.uppercase() },
    currencyCode = currencyCode,
    currencySymbol = currencySymbol,
    decimalPlaces = decimalPlaces,
    deliveryState = deliveryState.orEmpty(),
    taxOption = taxOption,
    dateFormat = dateFormat,
    showItemDescription = showItemDescription,
    showItemDiscount = showItemDiscount,
    internationalNumbering = internationalNumbering,
    roundOffMinor = roundOffMinor,
    additionalNotes = additionalNotes ?: notes.orEmpty(),
    termsAndConditions = termsAndConditions.orEmpty(),
    paymentDetails = paymentDetails,
    items = items.map { item -> item.toUiItem() },
    subtotalMinor = subtotalMinor,
    discountMinor = discountMinor,
    taxAmountMinor = taxAmountMinor,
    totalMinor = totalMinor
)

internal fun InvoiceItem.toUiItem(): InvoiceItemUiModel = InvoiceItemUiModel(
    id = id.toString(),
    name = name,
    description = description.orEmpty(),
    quantity = quantity.roundToInt(),
    unitPriceMinor = unitPriceMinor,
    discountPercent = discountPercent,
    cgstPercent = cgstPercent,
    sgstPercent = sgstPercent,
    igstPercent = igstPercent,
    lineSubtotalMinor = lineSubtotalMinor,
    discountMinor = discountMinor,
    taxableAmountMinor = taxableAmountMinor,
    taxAmountMinor = taxAmountMinor,
    totalMinor = totalMinor
)
