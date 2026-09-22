package com.example.freeinvoicegeneratorbydaybookcloud

import com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation.InvoiceCalculationEngine
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceCalculationEngineTest {
    private fun item(
        quantity: Double = 2.0,
        price: Long = 10_000L,
        discount: Double = 0.0,
        cgst: Double = 0.0,
        sgst: Double = 0.0,
        igst: Double = 0.0
    ) = InvoiceItem(
        name = "Service", quantity = quantity, unitPriceMinor = price,
        lineSubtotalMinor = 0L, discountPercent = discount,
        cgstPercent = cgst, sgstPercent = sgst, igstPercent = igst,
        taxAmountMinor = 0L, totalMinor = 0L
    )

    @Test fun simpleInvoiceIgnoresDiscountAndTax() {
        val result = InvoiceCalculationEngine.calculate(InvoiceType.SIMPLE, TaxOption.IGST,
            listOf(item(discount = 10.0, igst = 18.0)))
        assertEquals(20_000L, result.subtotalMinor)
        assertEquals(0L, result.discountMinor)
        assertEquals(0L, result.taxAmountMinor)
        assertEquals(20_000L, result.totalMinor)
    }

    @Test fun simpleInvoiceIncludesRoundOff() {
        val result = InvoiceCalculationEngine.calculate(InvoiceType.SIMPLE, TaxOption.NON_TAXABLE,
            listOf(item(quantity = 1.0)), roundOffMinor = 25L)
        assertEquals(25L, result.roundOffMinor)
        assertEquals(10_025L, result.totalMinor)
    }

    @Test fun cgstAndSgstAreCalculatedIndependentlyAfterDiscount() {
        val result = InvoiceCalculationEngine.calculate(InvoiceType.ADVANCED, TaxOption.CGST_SGST,
            listOf(item(discount = 10.0, cgst = 9.0, sgst = 9.0)))
        assertEquals(2_000L, result.discountMinor)
        assertEquals(3_240L, result.taxAmountMinor)
        assertEquals(21_240L, result.totalMinor)
        assertEquals(1_620L, result.items.single().cgstAmountMinor)
        assertEquals(1_620L, result.items.single().sgstAmountMinor)
    }

    @Test fun igstAndRoundOffAreIncluded() {
        val result = InvoiceCalculationEngine.calculate(InvoiceType.ADVANCED, TaxOption.IGST,
            listOf(item(quantity = 1.0, igst = 18.0)), roundOffMinor = 5L)
        assertEquals(1_800L, result.taxAmountMinor)
        assertEquals(11_805L, result.totalMinor)
    }

    @Test fun nonTaxableInvoiceHasNoTax() {
        val result = InvoiceCalculationEngine.calculate(InvoiceType.ADVANCED, TaxOption.NON_TAXABLE,
            listOf(item(igst = 18.0)))
        assertEquals(0L, result.taxAmountMinor)
        assertEquals(20_000L, result.totalMinor)
    }
}
