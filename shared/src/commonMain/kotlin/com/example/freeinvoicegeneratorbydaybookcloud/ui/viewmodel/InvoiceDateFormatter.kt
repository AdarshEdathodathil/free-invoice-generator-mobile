package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import com.example.freeinvoicegeneratorbydaybookcloud.shared.currentTimeMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal fun formattedInputDate(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }.take(3)
    return "$month ${date.dayOfMonth}, ${date.year}"
}

internal fun parseDate(value: String): Long = runCatching {
    val trimmed = value.trim()
    val monthEnd = trimmed.indexOf(' ')
    val comma = trimmed.indexOf(',')
    require(monthEnd > 0 && comma > monthEnd) { "invalid" }
    val month = monthNumber(trimmed.substring(0, monthEnd))
    val day = trimmed.substring(monthEnd + 1, comma).trim().toInt()
    val year = trimmed.substring(comma + 1).trim().toInt()
    LocalDate(year, month, day).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}.getOrDefault(currentTimeMillis())

internal fun formatInputDate(value: Long): String =
    formattedInputDate(Instant.fromEpochMilliseconds(value).toLocalDateTime(TimeZone.currentSystemDefault()).date)

internal fun formatStoredDate(value: Long, option: DateFormatOption): String {
    val date = Instant.fromEpochMilliseconds(value).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (option.pattern) {
        "dd/MM/yyyy" -> "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
        "MM/dd/yyyy" -> "${date.monthNumber.toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}/${date.year}"
        "yyyy-MM-dd" -> "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
        else -> "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    }
}

private fun monthNumber(name: String): Int = when (name.lowercase().take(3)) {
    "jan" -> 1
    "feb" -> 2
    "mar" -> 3
    "apr" -> 4
    "may" -> 5
    "jun" -> 6
    "jul" -> 7
    "aug" -> 8
    "sep" -> 9
    "oct" -> 10
    "nov" -> 11
    "dec" -> 12
    else -> 1
}
