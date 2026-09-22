package com.example.freeinvoicegeneratorbydaybookcloud

import com.example.freeinvoicegeneratorbydaybookcloud.util.amountInWords
import com.example.freeinvoicegeneratorbydaybookcloud.util.formatMoney
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun formatsInternationalAndIndianNumbering() {
        assertEquals("$1,234,567.89", formatMoney(123456789, "$", 2, internationalNumbering = true))
        assertEquals("₹12,34,567.89", formatMoney(123456789, "₹", 2, internationalNumbering = false))
    }

    @Test
    fun convertsAmountToWords() {
        assertEquals(
            "One thousand two hundred thirty four rupees and fifty six paise only",
            amountInWords(123456, "INR", 2)
        )
    }
}
