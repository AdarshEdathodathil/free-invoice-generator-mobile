package com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import kotlin.test.Test
import kotlin.test.assertEquals

class InvoiceCalculationEngineTest {
    @Test
    fun simpleInvoiceHasNoTaxOrDiscount() {
        val result = InvoiceCalculationEngine.calculate(
            invoiceType = InvoiceType.SIMPLE,
            taxOption = TaxOption.NON_TAXABLE,
            items = listOf(item(unitPriceMinor = 1_250, quantity = 2.0, discountPercent = 10.0, igstPercent = 18.0))
        )

        assertEquals(2_500, result.subtotalMinor)
        assertEquals(0, result.discountMinor)
        assertEquals(0, result.taxAmountMinor)
        assertEquals(2_500, result.totalMinor)
    }

    @Test
    fun advancedIgstInvoiceCalculatesDiscountThenTax() {
        val result = InvoiceCalculationEngine.calculate(
            invoiceType = InvoiceType.ADVANCED,
            taxOption = TaxOption.IGST,
            items = listOf(item(unitPriceMinor = 10_000, quantity = 1.0, discountPercent = 10.0, igstPercent = 18.0))
        )

        assertEquals(10_000, result.subtotalMinor)
        assertEquals(1_000, result.discountMinor)
        assertEquals(1_620, result.taxAmountMinor)
        assertEquals(10_620, result.totalMinor)
    }

    private fun item(
        unitPriceMinor: Long,
        quantity: Double,
        discountPercent: Double = 0.0,
        igstPercent: Double = 0.0
    ) = InvoiceItem(
        name = "Service",
        quantity = quantity,
        unitPriceMinor = unitPriceMinor,
        lineSubtotalMinor = 0L,
        discountPercent = discountPercent,
        igstPercent = igstPercent,
        taxAmountMinor = 0L,
        totalMinor = 0L
    )
}
