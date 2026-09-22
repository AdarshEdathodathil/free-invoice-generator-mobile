package com.example.freeinvoicegeneratorbydaybookcloud.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoiceItemUiModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoiceUiModel
import com.example.freeinvoicegeneratorbydaybookcloud.util.LogoResolver
import com.example.freeinvoicegeneratorbydaybookcloud.util.formatMoney
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class InvoicePdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logoResolver: LogoResolver
) {
    fun createSharePdf(invoice: InvoiceUiModel): Result<File> = runCatching {
        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(directory, safeFileName(invoice.invoiceNumber))
        writePdf(invoice, file)
        file
    }.onFailure { Log.e(TAG, "Unable to create invoice PDF for sharing.", it) }

    fun saveToDownloads(invoice: InvoiceUiModel): Result<Uri> = runCatching {
        val fileName = safeFileName(invoice.invoiceNumber)
        val tempFile = File(context.cacheDir, "invoices").let { directory ->
            directory.mkdirs()
            File(directory, fileName).also { writePdf(invoice, it) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, PDF_MIME_TYPE)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create Downloads entry.")
            resolver.openOutputStream(uri)?.use { output ->
                tempFile.inputStream().use { input -> input.copyTo(output) }
            } ?: error("Unable to open Downloads output stream.")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } else {
            val downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: error("Downloads directory is unavailable.")
            downloads.mkdirs()
            val target = File(downloads, fileName)
            tempFile.copyTo(target, overwrite = true)
            Uri.fromFile(target)
        }
    }.onFailure { Log.e(TAG, "Unable to save invoice PDF to Downloads.", it) }

    fun contentUriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun safeFileName(invoiceNumber: String): String {
        val safeNumber = invoiceNumber
            .replace(Regex("[^A-Za-z0-9._-]+"), "_")
            .trim('_', '.', '-')
            .ifBlank { "invoice" }
        return "invoice_$safeNumber.pdf"
    }

    private fun writePdf(invoice: InvoiceUiModel, file: File) {
        val document = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
            var canvas = page.canvas
            var y = margin

            fun newPageIfNeeded(heightNeeded: Float = 48f) {
                if (y + heightNeeded <= pageHeight - margin) return
                document.finishPage(page)
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                )
                canvas = page.canvas
                y = margin
            }

            fun text(
                value: String,
                x: Float = margin,
                size: Float = 11f,
                bold: Boolean = false,
                color: Int = Color.rgb(32, 40, 50),
                maxWidth: Float = pageWidth - margin * 2
            ) {
                if (value.isBlank()) return
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = size
                    this.color = color
                    typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
                }
                wrap(value, paint, maxWidth).forEach { line ->
                    newPageIfNeeded(size + 8f)
                    canvas.drawText(line, x, y, paint)
                    y += size + 5f
                }
            }

            fun section(title: String) {
                y += 8f
                text(title.uppercase(), size = 10f, bold = true, color = Color.rgb(75, 85, 99))
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(220, 225, 232)
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, y - 4f, pageWidth - margin, y - 4f, paint)
                y += 4f
            }

            val logo = logoResolver.decode(context, invoice.organizationLogoPath)
            logo?.let { drawLogo(canvas, it, margin, y) }
            text("INVOICE", x = if (logo != null) 130f else margin, size = 24f, bold = true, color = Color.rgb(25, 83, 190))
            text("# ${invoice.invoiceNumber}", x = if (logo != null) 130f else margin, size = 12f, color = Color.rgb(75, 85, 99))
            y = maxOf(y, margin + if (logo != null) 74f else 48f)

            section("Organization")
            text(invoice.organizationName, bold = true)
            text(invoice.organizationAddress)
            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                text(invoice.organizationCountry)
                text(invoice.organizationEmail)
                text(invoice.organizationMobile)
                text(invoice.organizationGstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" }.orEmpty())
                text(listOf(invoice.authorityName, invoice.authorityDesignation).filter { it.isNotBlank() }.joinToString(", "))
            }

            section("Customer")
            text(invoice.customerName, bold = true)
            text(invoice.customerAddress)
            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                text(invoice.customerCountry)
                text(invoice.customerMobile)
                text(invoice.customerEmail)
                text(invoice.customerGstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" }.orEmpty())
            }

            section("Invoice")
            text("Invoice number: ${invoice.invoiceNumber}")
            text("Invoice date: ${invoice.date}")
            text("Due date: ${invoice.dueDate}")
            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                text("Currency: ${invoice.currencyCode}")
                text("Delivery state: ${invoice.deliveryState}")
                text("Tax option: ${invoice.taxOption.displayName()}")
            }

            section("Items")
            invoice.items.forEachIndexed { index, item ->
                newPageIfNeeded(54f)
                text("${index + 1}. ${item.name}", bold = true)
                if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.showItemDescription) {
                    text(item.description, size = 10f)
                }
                text(
                    "Qty: ${item.quantity}    Price: ${money(invoice, item.unitPriceMinor)}    Total: ${money(invoice, item.totalMinor)}",
                    size = 10f
                )
                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                    if (invoice.showItemDiscount && item.discountMinor != 0L) {
                        text("Discount: ${money(invoice, item.discountMinor)}", size = 10f)
                    }
                    when (invoice.taxOption) {
                        TaxOption.CGST_SGST -> text("Tax: CGST ${item.cgstPercent}% + SGST ${item.sgstPercent}%", size = 10f)
                        TaxOption.IGST -> text("Tax: IGST ${item.igstPercent}%", size = 10f)
                        TaxOption.NON_TAXABLE -> Unit
                    }
                }
                y += 4f
            }

            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                section("Tax")
                when (invoice.taxOption) {
                    TaxOption.CGST_SGST -> {
                        val cgst = invoice.taxAmountMinor / 2
                        val sgst = invoice.taxAmountMinor - cgst
                        text("CGST: ${money(invoice, cgst)}")
                        text("SGST: ${money(invoice, sgst)}")
                    }
                    TaxOption.IGST -> text("IGST: ${money(invoice, invoice.taxAmountMinor)}")
                    TaxOption.NON_TAXABLE -> text("Non Taxable")
                }

                invoice.paymentDetails?.takeIf { it.paymentMethod != PaymentMethod.NONE }?.let { payment ->
                    section("Payment")
                    text("Account number: ${payment.accountNumber.orEmpty()}")
                    text("Account owner: ${payment.accountOwnerName.orEmpty()}")
                    text("Bank name: ${payment.bankName.orEmpty()}")
                    text("UPI ID: ${payment.upiId.orEmpty()}")
                }
            }

            section("Summary")
            text("Subtotal: ${money(invoice, invoice.subtotalMinor)}")
            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.discountMinor != 0L) {
                text("Discount: ${money(invoice, invoice.discountMinor)}")
            }
            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.taxAmountMinor != 0L) {
                text("Tax: ${money(invoice, invoice.taxAmountMinor)}")
            }
            text("Grand Total: ${money(invoice, invoice.totalMinor)}", size = 14f, bold = true, color = Color.rgb(25, 83, 190))
            if (invoice.roundOffMinor != 0L) {
                text("Round Off: ${money(invoice, invoice.roundOffMinor)}")
            }

            if (invoice.additionalNotes.isNotBlank()) {
                section("Notes")
                text(invoice.additionalNotes)
            }
            if (invoice.termsAndConditions.isNotBlank()) {
                section("Terms and Conditions")
                text(invoice.termsAndConditions)
            }

            document.finishPage(page)
            FileOutputStream(file).use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }

    private fun money(invoice: InvoiceUiModel, amount: Long): String =
        formatMoney(amount, invoice.currencySymbol, invoice.decimalPlaces, invoice.internationalNumbering)

    private fun drawLogo(canvas: Canvas, logo: Bitmap, x: Float, y: Float) {
        val maxSize = 72
        val scale = minOf(maxSize / logo.width.toFloat(), maxSize / logo.height.toFloat())
        val width = (logo.width * scale).toInt().coerceAtLeast(1)
        val height = (logo.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(logo, width, height, true)
        canvas.drawBitmap(scaled, x, y, null)
        if (scaled != logo) scaled.recycle()
    }

    private fun wrap(value: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        value.split('\n').forEach { paragraph ->
            var line = ""
            paragraph.split(' ').filter { it.isNotBlank() }.forEach { word ->
                val candidate = if (line.isBlank()) word else "$line $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    line = candidate
                } else {
                    if (line.isNotBlank()) lines += line
                    line = word
                }
            }
            if (line.isNotBlank()) lines += line
        }
        return lines.ifEmpty { listOf(value) }
    }

    private fun TaxOption.displayName(): String = when (this) {
        TaxOption.CGST_SGST -> "CGST & SGST"
        TaxOption.IGST -> "IGST"
        TaxOption.NON_TAXABLE -> "Non Taxable"
    }

    companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        private const val TAG = "InvoicePdfGenerator"
    }
}
