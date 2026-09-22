package com.example.freeinvoicegeneratorbydaybookcloud.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun formatMoney(
    amountMinor: Long,
    currencySymbol: String = "$",
    decimalPlaces: Int = 2,
    internationalNumbering: Boolean = true
): String {
    val scale = decimalPlaces.coerceIn(0, 3)
    val amount = BigDecimal.valueOf(amountMinor, scale)
    val locale = if (internationalNumbering) Locale.US else Locale.getDefault()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = scale
        maximumFractionDigits = scale
        isGroupingUsed = true
    }
    return "$currencySymbol${formatter.format(amount)}"
}
