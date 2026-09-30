package com.example.freeinvoicegeneratorbydaybookcloud.util

import kotlin.math.roundToLong

fun String.toMinor(decimalPlaces: Int): Long = runCatching {
    val trimmed = trim()
    if (trimmed.isEmpty()) return 0L
    val scale = decimalPlaces.coerceIn(0, 3)
    val negative = trimmed.startsWith("-")
    val normalized = trimmed.removePrefix("-")
    val parts = normalized.split('.')
    val whole = parts[0].toLongOrNull() ?: 0L
    val fractionRaw = parts.getOrNull(1).orEmpty()
    val fraction = when {
        scale == 0 -> 0L
        fractionRaw.length <= scale -> fractionRaw.padEnd(scale, '0').toLongOrNull() ?: 0L
        else -> fractionRaw.take(scale + 1).let {
            (it.dropLast(1).toLongOrNull() ?: 0L) +
                if (it.last().digitToInt() >= 5) 1L else 0L
        }
    }
    val divisor = (1..scale).fold(1L) { acc, _ -> acc * 10L }
    val amount = whole * divisor + fraction
    if (negative) -amount else amount
}.getOrDefault(0L)

fun minorToDecimalString(minor: Long, decimalPlaces: Int): String {
    val scale = decimalPlaces.coerceIn(0, 3)
    if (scale == 0) return minor.toString()
    val divisor = (1..scale).fold(1L) { acc, _ -> acc * 10L }
    val whole = minor / divisor
    val fraction = kotlin.math.abs(minor % divisor)
    return "$whole.${fraction.toString().padStart(scale, '0')}"
}

fun minorToInputString(minor: Long, decimalPlaces: Int): String = minorToDecimalString(minor, decimalPlaces)
