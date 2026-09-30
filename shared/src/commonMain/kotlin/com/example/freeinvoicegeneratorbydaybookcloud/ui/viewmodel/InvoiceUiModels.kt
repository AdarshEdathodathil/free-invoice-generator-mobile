package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation.InvoiceCalculation
import com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation.InvoiceCalculationEngine
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentDetails
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@OptIn(ExperimentalUuidApi::class)
data class InvoiceItemUiModel(
    val id: String = Uuid.random().toString(),
    val name: String,
    val description: String = "",
    val quantity: Int,
    val unitPriceMinor: Long,
    val discountPercent: Double = 0.0,
    val cgstPercent: Double = 0.0,
    val sgstPercent: Double = 0.0,
    val igstPercent: Double = 0.0,
    val lineSubtotalMinor: Long = quantity * unitPriceMinor,
    val discountMinor: Long = 0L,
    val taxableAmountMinor: Long = lineSubtotalMinor,
    val taxAmountMinor: Long = 0L,
    val totalMinor: Long = lineSubtotalMinor
) {
    val unitPriceCents: Long get() = unitPriceMinor
    val totalCents: Long get() = totalMinor
}

data class CreateInvoiceUiState(
    val currentStep: Int = 1,
    val invoiceType: InvoiceType = InvoiceType.SIMPLE,
    val organizationName: String = "",
    val organizationAddress: String = "",
    val organizationCountry: String = "",
    val organizationEmail: String = "",
    val organizationMobile: String = "",
    val organizationGstin: String = "",
    val authorityName: String = "",
    val authorityDesignation: String = "",
    val organizationLogoPath: String? = null,
    val customerName: String = "",
    val customerAddress: String = "",
    val customerCountry: String = "",
    val customerMobile: String = "",
    val customerEmail: String = "",
    val customerGstin: String = "",
    val invoiceNumber: String = "INV-001",
    val invoiceDate: String = formattedInputDate(Clock.System.todayIn(TimeZone.currentSystemDefault())),
    val dueDate: String = formattedInputDate(
        Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(DatePeriod(days = 15))
    ),
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val decimalPlaces: Int = 2,
    val deliveryState: String = "",
    val taxOption: TaxOption = TaxOption.NON_TAXABLE,
    val taxRatePercent: Int = 10,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val showItemDescription: Boolean = false,
    val showItemDiscount: Boolean = false,
    val internationalNumbering: Boolean = false,
    val roundOffMinor: Long = 0L,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val accountNumber: String = "",
    val accountOwnerName: String = "",
    val bankName: String = "",
    val upiId: String = "",
    val items: List<InvoiceItemUiModel> = emptyList(),
    val additionalNotes: String = "",
    val termsAndConditions: String = ""
) {
    val calculation: InvoiceCalculation
        get() = InvoiceCalculationEngine.calculate(
            invoiceType = invoiceType,
            taxOption = taxOption,
            items = items.map { it.toDomainInput() },
            roundOffMinor = roundOffMinor
        )
    val subtotalMinor: Long get() = calculation.subtotalMinor
    val discountMinor: Long get() = calculation.discountMinor
    val taxAmountMinor: Long get() = calculation.taxAmountMinor
    val totalMinor: Long get() = calculation.totalMinor
    val subtotalCents: Long get() = subtotalMinor
    val taxCents: Long get() = taxAmountMinor
    val totalCents: Long get() = totalMinor
}

data class InvoiceUiModel(
    val id: Long,
    val invoiceNumber: String,
    val invoiceType: InvoiceType,
    val organizationName: String,
    val organizationAddress: String,
    val organizationCountry: String,
    val organizationEmail: String,
    val organizationMobile: String,
    val organizationGstin: String,
    val authorityName: String,
    val authorityDesignation: String,
    val organizationLogoPath: String?,
    val customerName: String,
    val customerAddress: String,
    val customerCountry: String,
    val customerMobile: String,
    val customerEmail: String,
    val customerGstin: String,
    val date: String,
    val dueDate: String,
    val amountMinor: Long,
    val status: String,
    val currencyCode: String,
    val currencySymbol: String,
    val decimalPlaces: Int,
    val deliveryState: String,
    val taxOption: TaxOption,
    val dateFormat: DateFormatOption,
    val showItemDescription: Boolean,
    val showItemDiscount: Boolean,
    val internationalNumbering: Boolean,
    val roundOffMinor: Long,
    val additionalNotes: String,
    val termsAndConditions: String,
    val paymentDetails: PaymentDetails?,
    val items: List<InvoiceItemUiModel>,
    val subtotalMinor: Long,
    val discountMinor: Long,
    val taxAmountMinor: Long,
    val totalMinor: Long
) {
    val amountCents: Long get() = amountMinor
    val subtotalCents: Long get() = subtotalMinor
    val taxCents: Long get() = taxAmountMinor
    val totalCents: Long get() = totalMinor
    val taxRatePercent: Int get() = when (taxOption) {
        TaxOption.CGST_SGST -> items.firstOrNull()?.let { (it.cgstPercent + it.sgstPercent).roundToInt() } ?: 0
        TaxOption.IGST -> items.firstOrNull()?.igstPercent?.roundToInt() ?: 0
        TaxOption.NON_TAXABLE -> 0
    }
}

data class PdfActionState(
    val isGeneratingPdf: Boolean = false
)

sealed interface InvoicePreviewEvent {
    data class DownloadSuccess(val location: String) : InvoicePreviewEvent
    data object DownloadError : InvoicePreviewEvent
    data class ShareReady(val filePath: String, val fileName: String) : InvoicePreviewEvent
    data object ShareError : InvoicePreviewEvent
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
