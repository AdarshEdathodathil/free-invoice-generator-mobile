package com.example.freeinvoicegeneratorbydaybookcloud.data.local.converter

import androidx.room.TypeConverter
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption

class InvoiceConfigurationConverters {
    @TypeConverter fun invoiceTypeToString(value: InvoiceType): String = value.name
    @TypeConverter fun stringToInvoiceType(value: String): InvoiceType =
        runCatching { InvoiceType.valueOf(value) }.getOrDefault(InvoiceType.SIMPLE)

    @TypeConverter fun taxOptionToString(value: TaxOption): String = value.name
    @TypeConverter fun stringToTaxOption(value: String): TaxOption =
        runCatching { TaxOption.valueOf(value) }.getOrDefault(TaxOption.NON_TAXABLE)

    @TypeConverter fun dateFormatToString(value: DateFormatOption): String = value.name
    @TypeConverter fun stringToDateFormat(value: String): DateFormatOption =
        runCatching { DateFormatOption.valueOf(value) }.getOrDefault(DateFormatOption.DD_MM_YYYY)

    @TypeConverter fun paymentMethodToString(value: PaymentMethod): String = value.name
    @TypeConverter fun stringToPaymentMethod(value: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.NONE)
}
