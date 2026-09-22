package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val inputDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

internal fun formattedInputDate(date: LocalDate): String = date.format(inputDateFormatter)

internal fun parseDate(value: String): Long = try {
    LocalDate.parse(value.trim(), inputDateFormatter)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

internal fun formatInputDate(value: Long): String = formattedInputDate(
    Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
)

internal fun formatStoredDate(value: Long, option: DateFormatOption): String =
    Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern(option.pattern, Locale.US))
