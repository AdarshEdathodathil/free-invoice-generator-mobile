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
    if (!internationalNumbering) {
        return "$currencySymbol${formatIndianNumber(amount, scale)}"
    }
    val locale = Locale.US
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = scale
        maximumFractionDigits = scale
        isGroupingUsed = true
    }
    return "$currencySymbol${formatter.format(amount)}"
}

fun amountInWords(
    amountMinor: Long,
    currencyCode: String,
    decimalPlaces: Int = 2
): String {
    val scale = decimalPlaces.coerceIn(0, 3)
    val absolute = kotlin.math.abs(amountMinor)
    val divisor = pow10(scale)
    val major = if (scale == 0) absolute else absolute / divisor
    val minor = if (scale == 0) 0L else absolute % divisor
    val currencyName = currencyMajorName(currencyCode)
    val minorName = currencyMinorName(currencyCode)
    val prefix = if (amountMinor < 0) "Minus " else ""
    val majorWords = numberToWords(major).replaceFirstChar { it.uppercase() }
    val minorWords = if (scale > 0 && minor > 0) {
        " and ${numberToWords(minor)} $minorName"
    } else {
        ""
    }
    return "$prefix$majorWords $currencyName$minorWords only"
}

private fun formatIndianNumber(amount: BigDecimal, scale: Int): String {
    val plain = amount.setScale(scale).toPlainString()
    val parts = plain.split('.')
    val whole = parts[0]
    val sign = if (whole.startsWith("-")) "-" else ""
    val digits = whole.removePrefix("-")
    if (digits.length <= 3) return plain
    val lastThree = digits.takeLast(3)
    val leading = digits.dropLast(3)
    val groupedLeading = leading.reversed().chunked(2).joinToString(",").reversed()
    val decimal = parts.getOrNull(1)?.let { ".$it" }.orEmpty()
    return "$sign$groupedLeading,$lastThree$decimal"
}

private fun pow10(scale: Int): Long = (1..scale).fold(1L) { acc, _ -> acc * 10L }

private fun currencyMajorName(code: String): String = when (code.uppercase(Locale.US)) {
    "INR" -> "rupees"
    "USD" -> "dollars"
    "EUR" -> "euros"
    "GBP" -> "pounds"
    "JPY" -> "yen"
    "AED" -> "dirhams"
    "SAR" -> "riyals"
    else -> code.uppercase(Locale.US)
}

private fun currencyMinorName(code: String): String = when (code.uppercase(Locale.US)) {
    "INR" -> "paise"
    "USD", "AUD", "CAD", "SGD", "NZD", "HKD" -> "cents"
    "EUR" -> "cents"
    "GBP" -> "pence"
    "AED" -> "fils"
    "SAR" -> "halalas"
    else -> "minor units"
}

private fun numberToWords(value: Long): String {
    if (value == 0L) return "zero"
    val units = listOf(
        1_000_000_000L to "billion",
        1_000_000L to "million",
        1_000L to "thousand",
        100L to "hundred"
    )
    var remaining = value
    val parts = mutableListOf<String>()
    for ((unitValue, unitName) in units) {
        if (remaining >= unitValue) {
            parts += "${numberToWords(remaining / unitValue)} $unitName"
            remaining %= unitValue
        }
    }
    if (remaining > 0) {
        parts += underHundredToWords(remaining.toInt())
    }
    return parts.joinToString(" ")
}

private fun underHundredToWords(value: Int): String {
    val small = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )
    val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
    return when {
        value < 20 -> small[value]
        value % 10 == 0 -> tens[value / 10]
        else -> "${tens[value / 10]} ${small[value % 10]}"
    }
}
