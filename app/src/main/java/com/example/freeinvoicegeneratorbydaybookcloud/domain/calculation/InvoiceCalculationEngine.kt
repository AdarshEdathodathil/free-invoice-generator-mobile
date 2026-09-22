package com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import java.math.BigDecimal
import java.math.RoundingMode

data class InvoiceCalculation(
    val items: List<InvoiceItem>,
    val subtotalMinor: Long,
    val discountMinor: Long,
    val taxAmountMinor: Long,
    val roundOffMinor: Long,
    val totalMinor: Long
)

object InvoiceCalculationEngine {
    fun calculate(
        invoiceType: InvoiceType,
        taxOption: TaxOption,
        items: List<InvoiceItem>,
        roundOffMinor: Long = 0L
    ): InvoiceCalculation {
        val calculatedItems = items.map { item ->
            calculateItem(invoiceType, taxOption, item)
        }
        val subtotal = calculatedItems.sumOf { it.lineSubtotalMinor }
        val discount = calculatedItems.sumOf { it.discountMinor }
        val tax = calculatedItems.sumOf { it.taxAmountMinor }
        return InvoiceCalculation(
            items = calculatedItems,
            subtotalMinor = subtotal,
            discountMinor = discount,
            taxAmountMinor = tax,
            roundOffMinor = roundOffMinor,
            totalMinor = subtotal - discount + tax + roundOffMinor
        )
    }

    private fun calculateItem(
        invoiceType: InvoiceType,
        taxOption: TaxOption,
        item: InvoiceItem
    ): InvoiceItem {
        val subtotal = multiply(item.unitPriceMinor, item.quantity)
        if (invoiceType == InvoiceType.SIMPLE) {
            return item.copy(
                lineSubtotalMinor = subtotal,
                discountPercent = 0.0,
                discountMinor = 0L,
                taxableAmountMinor = subtotal,
                cgstPercent = 0.0,
                cgstAmountMinor = 0L,
                sgstPercent = 0.0,
                sgstAmountMinor = 0L,
                igstPercent = 0.0,
                igstAmountMinor = 0L,
                taxPercent = 0.0,
                taxAmountMinor = 0L,
                totalMinor = subtotal
            )
        }

        val discount = percentage(subtotal, item.discountPercent)
        val taxable = (subtotal - discount).coerceAtLeast(0L)
        val cgst = if (taxOption == TaxOption.CGST_SGST) percentage(taxable, item.cgstPercent) else 0L
        val sgst = if (taxOption == TaxOption.CGST_SGST) percentage(taxable, item.sgstPercent) else 0L
        val igst = if (taxOption == TaxOption.IGST) percentage(taxable, item.igstPercent) else 0L
        val tax = cgst + sgst + igst
        return item.copy(
            lineSubtotalMinor = subtotal,
            discountMinor = discount,
            taxableAmountMinor = taxable,
            cgstAmountMinor = cgst,
            sgstAmountMinor = sgst,
            igstAmountMinor = igst,
            taxPercent = when (taxOption) {
                TaxOption.CGST_SGST -> item.cgstPercent + item.sgstPercent
                TaxOption.IGST -> item.igstPercent
                TaxOption.NON_TAXABLE -> 0.0
            },
            taxAmountMinor = tax,
            totalMinor = taxable + tax
        )
    }

    private fun multiply(amountMinor: Long, quantity: Double): Long =
        BigDecimal.valueOf(amountMinor)
            .multiply(BigDecimal.valueOf(quantity))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()

    private fun percentage(amountMinor: Long, percent: Double): Long =
        BigDecimal.valueOf(amountMinor)
            .multiply(BigDecimal.valueOf(percent))
            .divide(BigDecimal.valueOf(100L), 0, RoundingMode.HALF_UP)
            .longValueExact()
}
