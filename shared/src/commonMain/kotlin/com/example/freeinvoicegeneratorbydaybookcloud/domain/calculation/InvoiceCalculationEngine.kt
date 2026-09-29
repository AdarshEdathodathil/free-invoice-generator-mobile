package com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import kotlin.math.roundToLong

data class InvoiceCalculation(
    val items: List<InvoiceItem>, val subtotalMinor: Long, val discountMinor: Long,
    val taxAmountMinor: Long, val roundOffMinor: Long, val totalMinor: Long
)

/** Shared deterministic money calculation for Android and iOS. */
object InvoiceCalculationEngine {
    fun calculate(invoiceType: InvoiceType, taxOption: TaxOption, items: List<InvoiceItem>, roundOffMinor: Long = 0L): InvoiceCalculation {
        val calculated = items.map { calculateItem(invoiceType, taxOption, it) }
        val subtotal = calculated.sumOf { it.lineSubtotalMinor }
        val discount = calculated.sumOf { it.discountMinor }
        val tax = calculated.sumOf { it.taxAmountMinor }
        return InvoiceCalculation(calculated, subtotal, discount, tax, roundOffMinor, subtotal - discount + tax + roundOffMinor)
    }

    private fun calculateItem(invoiceType: InvoiceType, taxOption: TaxOption, item: InvoiceItem): InvoiceItem {
        val subtotal = multiply(item.unitPriceMinor, item.quantity)
        if (invoiceType == InvoiceType.SIMPLE) return item.copy(
            lineSubtotalMinor = subtotal, discountPercent = 0.0, discountMinor = 0L, taxableAmountMinor = subtotal,
            cgstPercent = 0.0, cgstAmountMinor = 0L, sgstPercent = 0.0, sgstAmountMinor = 0L,
            igstPercent = 0.0, igstAmountMinor = 0L, taxPercent = 0.0, taxAmountMinor = 0L, totalMinor = subtotal
        )
        val discount = percentage(subtotal, item.discountPercent)
        val taxable = (subtotal - discount).coerceAtLeast(0L)
        val cgst = if (taxOption == TaxOption.CGST_SGST) percentage(taxable, item.cgstPercent) else 0L
        val sgst = if (taxOption == TaxOption.CGST_SGST) percentage(taxable, item.sgstPercent) else 0L
        val igst = if (taxOption == TaxOption.IGST) percentage(taxable, item.igstPercent) else 0L
        val tax = cgst + sgst + igst
        return item.copy(
            lineSubtotalMinor = subtotal, discountMinor = discount, taxableAmountMinor = taxable,
            cgstAmountMinor = cgst, sgstAmountMinor = sgst, igstAmountMinor = igst,
            taxPercent = when (taxOption) { TaxOption.CGST_SGST -> item.cgstPercent + item.sgstPercent; TaxOption.IGST -> item.igstPercent; TaxOption.NON_TAXABLE -> 0.0 },
            taxAmountMinor = tax, totalMinor = taxable + tax
        )
    }

    private fun multiply(amountMinor: Long, quantity: Double): Long = (amountMinor.toDouble() * quantity).roundToLong()
    private fun percentage(amountMinor: Long, percent: Double): Long = (amountMinor.toDouble() * percent / 100).roundToLong()
}
