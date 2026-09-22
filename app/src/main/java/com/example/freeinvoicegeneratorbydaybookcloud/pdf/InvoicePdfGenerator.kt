package com.example.freeinvoicegeneratorbydaybookcloud.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
import com.example.freeinvoicegeneratorbydaybookcloud.util.amountInWords
import com.example.freeinvoicegeneratorbydaybookcloud.util.formatMoney
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class InvoicePdfGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
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
            val margin = 36f
            val contentWidth = pageWidth - margin * 2
            val primaryColor = Color.rgb(13, 130, 115)
            val primaryDark = Color.rgb(15, 23, 42)
            val bodyColor = Color.rgb(31, 41, 55)
            val mutedColor = Color.rgb(92, 103, 116)
            val lineColor = Color.rgb(223, 230, 238)
            val softColor = Color.rgb(245, 248, 251)
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

            fun makePaint(
                size: Float = 12f,
                bold: Boolean = false,
                color: Int = bodyColor
            ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                this.color = color
                typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            }

            fun drawWrapped(
                value: String,
                x: Float = margin,
                startY: Float = y,
                width: Float = contentWidth,
                size: Float = 12f,
                bold: Boolean = false,
                color: Int = bodyColor,
                maxLines: Int = Int.MAX_VALUE
            ): Float {
                if (value.isBlank()) return startY
                val paint = makePaint(size, bold, color)
                var lineY = startY
                wrap(value, paint, width).take(maxLines).forEach { line ->
                    canvas.drawText(line, x, lineY, paint)
                    lineY += size + 5f
                }
                return lineY
            }

            fun text(
                value: String,
                x: Float = margin,
                size: Float = 12f,
                bold: Boolean = false,
                color: Int = bodyColor,
                maxWidth: Float = contentWidth
            ) {
                if (value.isBlank()) return
                val paint = makePaint(size, bold, color)
                wrap(value, paint, maxWidth).forEach { line ->
                    newPageIfNeeded(size + 10f)
                    canvas.drawText(line, x, y, paint)
                    y += size + 6f
                }
            }

            fun section(title: String) {
                newPageIfNeeded(34f)
                y += 8f
                canvas.drawText(title.uppercase(), margin, y, makePaint(11f, true, mutedColor))
                y += 8f
                canvas.drawLine(margin, y, pageWidth - margin, y, makePaint(1f, false, lineColor).apply { strokeWidth = 1.4f })
                y += 14f
            }

            fun drawRight(value: String, right: Float, baseline: Float, size: Float = 12f, bold: Boolean = false, color: Int = bodyColor) {
                val paint = makePaint(size, bold, color)
                canvas.drawText(value, right - paint.measureText(value), baseline, paint)
            }

            fun labeledLine(label: String, value: String, x: Float, startY: Float, width: Float): Float {
                if (value.isBlank()) return startY
                val labelPaint = makePaint(10.5f, true, mutedColor)
                val valuePaint = makePaint(12f, false, bodyColor)
                canvas.drawText("$label: ", x, startY, labelPaint)
                val labelWidth = labelPaint.measureText("$label: ")
                return drawWrapped(value, x + labelWidth, startY, width - labelWidth, 12f, false, bodyColor)
            }

            fun drawPartyBlock(title: String, lines: List<String>, x: Float, top: Float, width: Float): Float {
                var blockY = top + 22f
                canvas.drawText(title.uppercase(), x, blockY, makePaint(10.5f, true, mutedColor))
                blockY += 22f
                lines.filter { it.isNotBlank() }.forEachIndexed { index, line ->
                    blockY = drawWrapped(
                        value = line,
                        x = x,
                        startY = blockY,
                        width = width,
                        size = if (index == 0) 13.5f else 12f,
                        bold = index == 0,
                        color = if (index == 0) primaryDark else bodyColor
                    )
                }
                return blockY
            }

            fun detailChip(label: String, value: String, x: Float, top: Float, width: Float): Float {
                canvas.drawRoundRect(RectF(x, top, x + width, top + 52f), 10f, 10f, makePaint(color = softColor))
                canvas.drawText(label.uppercase(), x + 12f, top + 18f, makePaint(9.5f, true, mutedColor))
                drawWrapped(value, x + 12f, top + 38f, width - 24f, 12f, true, primaryDark, maxLines = 1)
                return top + 52f
            }

            val logo = logoResolver.decode(context, invoice.organizationLogoPath)
            canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 92f), 12f, 12f, makePaint(color = primaryColor))
            canvas.drawText("INVOICE", margin + 20f, y + 38f, makePaint(27f, true, Color.WHITE))
            canvas.drawText("# ${invoice.invoiceNumber}", margin + 20f, y + 66f, makePaint(13.5f, false, Color.WHITE))
            logo?.let { drawLogo(canvas, it, pageWidth - margin - 82f, y + 15f, 72) }
            y += 116f

            val columnGap = 18f
            val columnWidth = (contentWidth - columnGap) / 2f
            val partyTop = y
            canvas.drawRoundRect(RectF(margin, partyTop, margin + columnWidth, partyTop + 140f), 10f, 10f, makePaint(color = softColor))
            canvas.drawRoundRect(RectF(margin + columnWidth + columnGap, partyTop, pageWidth - margin, partyTop + 140f), 10f, 10f, makePaint(color = softColor))

            val organizationLines = buildList {
                add(invoice.organizationName)
                add(invoice.organizationAddress)
                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                    add(invoice.organizationCountry)
                    add(invoice.organizationEmail)
                    add(invoice.organizationMobile)
                    add(invoice.organizationGstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" }.orEmpty())
                    add(listOf(invoice.authorityName, invoice.authorityDesignation).filter { it.isNotBlank() }.joinToString(", "))
                }
            }
            val customerLines = buildList {
                add(invoice.customerName)
                add(invoice.customerAddress)
                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                    add(invoice.customerCountry)
                    add(invoice.customerMobile)
                    add(invoice.customerEmail)
                    add(invoice.customerGstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" }.orEmpty())
                }
            }
            val fromEnd = drawPartyBlock("From", organizationLines, margin + 14f, partyTop, columnWidth - 28f)
            val billToEnd = drawPartyBlock("Bill To", customerLines, margin + columnWidth + columnGap + 14f, partyTop, columnWidth - 28f)
            y = maxOf(partyTop + 152f, fromEnd + 14f, billToEnd + 14f)

            newPageIfNeeded(72f)
            val detailTop = y
            val chipWidth = (contentWidth - 24f) / 3f
            detailChip("Invoice Date", invoice.date, margin, detailTop, chipWidth)
            detailChip("Due Date", invoice.dueDate, margin + chipWidth + 12f, detailTop, chipWidth)
            detailChip("Currency", invoice.currencyCode, margin + (chipWidth + 12f) * 2f, detailTop, chipWidth)
            y = detailTop + 70f
            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                section("Invoice Details")
                y = labeledLine("Delivery state", invoice.deliveryState, margin, y, contentWidth)
                y = labeledLine("Tax option", invoice.taxOption.displayName(), margin, y, contentWidth)
                y += 8f
            }

            section("Items")
            val descX = margin
            val qtyX = margin + 250f
            val priceRight = margin + 408f
            val totalRight = pageWidth - margin
            fun drawItemsHeader() {
                canvas.drawRoundRect(RectF(margin, y - 18f, pageWidth - margin, y + 14f), 8f, 8f, makePaint(color = Color.rgb(221, 247, 242)))
                canvas.drawText("Description", descX + 10f, y + 3f, makePaint(11f, true, mutedColor))
                canvas.drawText("Qty", qtyX, y + 3f, makePaint(11f, true, mutedColor))
                drawRight("Price", priceRight, y + 3f, 11f, true, mutedColor)
                drawRight("Total", totalRight - 10f, y + 3f, 11f, true, mutedColor)
                y += 28f
            }
            drawItemsHeader()
            invoice.items.forEachIndexed { index, item ->
                newPageIfNeeded(76f)
                if (y < margin + 28f) {
                    section("Items")
                    drawItemsHeader()
                }
                val rowTop = y - 10f
                val nameLines = wrap("${index + 1}. ${item.name}", makePaint(12.5f, true), 220f)
                val descLines = if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.showItemDescription && item.description.isNotBlank()) {
                    wrap(item.description, makePaint(10.5f), 220f)
                } else {
                    emptyList()
                }
                val lineCount = nameLines.size + descLines.size
                val rowHeight = maxOf(58f, 24f + lineCount * 16f)
                canvas.drawLine(margin, rowTop, pageWidth - margin, rowTop, makePaint(color = lineColor).apply { strokeWidth = 1f })
                var rowY = y + 8f
                nameLines.forEach { line ->
                    canvas.drawText(line, descX + 10f, rowY, makePaint(12.5f, true, primaryDark))
                    rowY += 16f
                }
                descLines.forEach { line ->
                    canvas.drawText(line, descX + 10f, rowY, makePaint(10.5f, false, mutedColor))
                    rowY += 15f
                }
                canvas.drawText(item.quantity.toString(), qtyX, y + 8f, makePaint(12f, false, bodyColor))
                drawRight(money(invoice, item.unitPriceMinor), priceRight, y + 8f, 11.5f, false, bodyColor)
                drawRight(money(invoice, item.totalMinor), totalRight - 10f, y + 8f, 12f, true, primaryDark)
                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                    if (invoice.showItemDiscount && item.discountMinor != 0L) {
                        canvas.drawText("Discount: ${money(invoice, item.discountMinor)}", qtyX, y + 27f, makePaint(10f, false, mutedColor))
                    }
                    when (invoice.taxOption) {
                        TaxOption.CGST_SGST -> canvas.drawText("Tax: CGST ${item.cgstPercent}% + SGST ${item.sgstPercent}%", qtyX, y + 43f, makePaint(10f, false, mutedColor))
                        TaxOption.IGST -> canvas.drawText("Tax: IGST ${item.igstPercent}%", qtyX, y + 43f, makePaint(10f, false, mutedColor))
                        TaxOption.NON_TAXABLE -> Unit
                    }
                }
                y += rowHeight
            }
            y += 8f

            if (invoice.invoiceType == InvoiceType.ADVANCED) {
                section("Tax")
                when (invoice.taxOption) {
                    TaxOption.CGST_SGST -> {
                        val cgst = invoice.taxAmountMinor / 2
                        val sgst = invoice.taxAmountMinor - cgst
                        text("CGST: ${money(invoice, cgst)}", size = 12.5f)
                        text("SGST: ${money(invoice, sgst)}", size = 12.5f)
                    }
                    TaxOption.IGST -> text("IGST: ${money(invoice, invoice.taxAmountMinor)}", size = 12.5f)
                    TaxOption.NON_TAXABLE -> text("Non Taxable", size = 12.5f)
                }

                invoice.paymentDetails?.takeIf { it.paymentMethod != PaymentMethod.NONE }?.let { payment ->
                    section("Payment")
                    y = labeledLine("Account number", payment.accountNumber.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("Account owner", payment.accountOwnerName.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("Bank name", payment.bankName.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("UPI ID", payment.upiId.orEmpty(), margin, y, contentWidth)
                    y += 8f
                }
            }

            section("Summary")
            newPageIfNeeded(120f)
            fun summaryRow(label: String, value: String, bold: Boolean = false, color: Int = bodyColor, size: Float = 12.5f) {
                canvas.drawText(label, margin + 250f, y, makePaint(size, bold, mutedColor))
                drawRight(value, pageWidth - margin, y, size, bold, color)
                y += size + 8f
            }
            summaryRow("Subtotal", money(invoice, invoice.subtotalMinor))
            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.discountMinor != 0L) {
                summaryRow("Discount", money(invoice, invoice.discountMinor))
            }
            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.taxAmountMinor != 0L) {
                summaryRow("Tax", money(invoice, invoice.taxAmountMinor))
            }
            if (invoice.roundOffMinor != 0L) {
                summaryRow("Round Off", money(invoice, invoice.roundOffMinor))
            }
            y += 4f
            canvas.drawLine(margin + 250f, y, pageWidth - margin, y, makePaint(color = lineColor).apply { strokeWidth = 1.4f })
            y += 22f
            summaryRow("Grand Total", money(invoice, invoice.totalMinor), bold = true, color = primaryColor, size = 16f)
            y += 8f
            canvas.drawText("AMOUNT IN WORDS", margin, y, makePaint(10.5f, true, mutedColor))
            y += 20f
            text(amountInWords(invoice.totalMinor, invoice.currencyCode, invoice.decimalPlaces), size = 13.5f, bold = true, color = primaryDark)

            if (invoice.additionalNotes.isNotBlank()) {
                section("Notes")
                text(invoice.additionalNotes, size = 12.5f)
            }
            if (invoice.termsAndConditions.isNotBlank()) {
                section("Terms and Conditions")
                text(invoice.termsAndConditions, size = 12.5f)
            }

            document.finishPage(page)
            FileOutputStream(file).use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }

    private fun money(invoice: InvoiceUiModel, amount: Long): String =
        formatMoney(amount, invoice.currencySymbol, invoice.decimalPlaces, invoice.internationalNumbering)

    private fun drawLogo(canvas: Canvas, logo: Bitmap, x: Float, y: Float, maxSize: Int = 72) {
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
