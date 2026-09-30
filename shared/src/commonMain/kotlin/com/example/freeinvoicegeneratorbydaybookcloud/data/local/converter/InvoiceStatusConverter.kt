package com.example.freeinvoicegeneratorbydaybookcloud.data.local.converter

import androidx.room.TypeConverter
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus

/**
 * Room TypeConverter for [InvoiceStatus] ↔ String.
 * Registered on [com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase]
 * via [@TypeConverters].
 */
class InvoiceStatusConverter {

    @TypeConverter
    fun fromStatus(status: InvoiceStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): InvoiceStatus = InvoiceStatus.fromString(value)
}

