package com.example.freeinvoicegeneratorbydaybookcloud.util

fun formatMoney(
    amountMinor: Long,
    currencySymbol: String = "$",
    decimalPlaces: Int = 2,
    internationalNumbering: Boolean = true
): String {
    val scale = decimalPlaces.coerceIn(0, 3)
    val divisor = pow10(scale)
    val sign = if (amountMinor < 0) "-" else ""
    val absolute = kotlin.math.abs(amountMinor)
    val whole = if (scale == 0) absolute else absolute / divisor
    val fraction = if (scale == 0) 0L else absolute % divisor
    val wholeFormatted = if (internationalNumbering) formatWestern(whole) else formatIndian(whole, scale, fraction)
    if (!internationalNumbering) return "$currencySymbol$sign$wholeFormatted"
    val decimal = if (scale == 0) "" else "." + fraction.toString().padStart(scale, '0')
    return "$currencySymbol$sign$wholeFormatted$decimal"
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
    val majorWords = numberToWords(major).replaceFirstChar { it.uppercaseChar() }
    val minorWords = if (scale > 0 && minor > 0) {
        " and ${numberToWords(minor)} $minorName"
    } else {
        ""
    }
    return "$prefix$majorWords $currencyName$minorWords only"
}

private fun formatWestern(value: Long): String {
    val digits = value.toString()
    if (digits.length <= 3) return digits
    val reversed = digits.reversed()
    val grouped = buildString {
        reversed.forEachIndexed { index, char ->
            if (index != 0 && index % 3 == 0) append(',')
            append(char)
        }
    }
    return grouped.reversed()
}

private fun formatIndian(whole: Long, scale: Int, fraction: Long): String {
    val digits = whole.toString()
    if (digits.length <= 3) {
        val decimal = if (scale == 0) "" else "." + fraction.toString().padStart(scale, '0')
        return digits + decimal
    }
    val lastThree = digits.takeLast(3)
    val leading = digits.dropLast(3)
    val groupedLeading = leading.reversed().chunked(2).joinToString(",").reversed()
    val decimal = if (scale == 0) "" else "." + fraction.toString().padStart(scale, '0')
    return "$groupedLeading,$lastThree$decimal"
}

private fun pow10(scale: Int): Long = (1..scale).fold(1L) { acc, _ -> acc * 10L }

private fun currencyMajorName(code: String): String = when (code.uppercase()) {
    "INR" -> "rupees"
    "USD" -> "dollars"
    "EUR" -> "euros"
    "GBP" -> "pounds"
    "JPY" -> "yen"
    "AED" -> "dirhams"
    "SAR" -> "riyals"
    else -> code.uppercase()
}

private fun currencyMinorName(code: String): String = when (code.uppercase()) {
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
