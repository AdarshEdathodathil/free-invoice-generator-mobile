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
    fun createSharePdf(invoice: InvoiceUiModel, templateId: String): Result<File> = runCatching {
        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(directory, safeFileName(invoice.invoiceNumber))
        writePdf(invoice, file, templateId)
        file
    }.onFailure { Log.e(TAG, "Unable to create invoice PDF for sharing.", it) }

    fun saveToDownloads(invoice: InvoiceUiModel, templateId: String): Result<Uri> = runCatching {
        val fileName = safeFileName(invoice.invoiceNumber)
        val tempFile = File(context.cacheDir, "invoices").let { directory ->
            directory.mkdirs()
            File(directory, fileName).also { writePdf(invoice, it, templateId) }
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

    private fun writePdf(invoice: InvoiceUiModel, file: File, templateId: String) {
        val document = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 36f
            val contentWidth = pageWidth - margin * 2
            val templateStyle = pdfTemplateStyle(templateId)
            val templateLayout = pdfTemplateLayout(templateId)
            val primaryColor = templateStyle.headerColor
            val onPrimaryColor = templateStyle.onHeaderColor
            val accentColor = templateStyle.accentColor
            val primaryDark = Color.rgb(15, 23, 42)
            val bodyColor = Color.rgb(31, 41, 55)
            val mutedColor = Color.rgb(92, 103, 116)
            val lineColor = Color.rgb(223, 230, 238)
            val softColor = Color.rgb(245, 248, 251)
            val alternatingRowColor = Color.rgb(250, 252, 255)
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
            fun drawHeaderLogo(x: Float, top: Float, size: Int = 72) {
                logo?.let { drawLogo(canvas, it, x, top, size) }
            }

            when (templateLayout) {
                PdfTemplateLayout.CENTERED -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 116f), 12f, 12f, makePaint(color = primaryColor))
                    logo?.let { drawLogo(canvas, it, pageWidth / 2f - 34f, y + 14f, 68) }
                    drawCentered(canvas, "INVOICE", pageWidth / 2f, y + 72f, makePaint(27f, true, onPrimaryColor))
                    drawCentered(canvas, "# ${invoice.invoiceNumber}", pageWidth / 2f, y + 96f, makePaint(13.5f, false, onPrimaryColor))
                    y += 140f
                }
                PdfTemplateLayout.SIDE_RAIL -> {
                    canvas.drawRoundRect(RectF(margin, y, margin + 18f, y + 92f), 9f, 9f, makePaint(color = primaryColor))
                    canvas.drawText("INVOICE", margin + 38f, y + 38f, makePaint(27f, true, primaryColor))
                    canvas.drawText("# ${invoice.invoiceNumber}", margin + 38f, y + 66f, makePaint(13.5f, false, mutedColor))
                    drawHeaderLogo(pageWidth - margin - 82f, y + 15f)
                    y += 116f
                }
                PdfTemplateLayout.TOP_STRIPE, PdfTemplateLayout.LEDGER -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 8f), 4f, 4f, makePaint(color = primaryColor))
                    canvas.drawText("INVOICE", margin, y + 48f, makePaint(27f, true, primaryColor))
                    canvas.drawText("# ${invoice.invoiceNumber}", margin, y + 74f, makePaint(13.5f, false, mutedColor))
                    drawHeaderLogo(pageWidth - margin - 82f, y + 25f)
                    y += 108f
                }
                PdfTemplateLayout.TOTAL_HERO -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 96f), 12f, 12f, makePaint(color = primaryColor))
                    canvas.drawText("INVOICE", margin + 20f, y + 36f, makePaint(24f, true, onPrimaryColor))
                    canvas.drawText("# ${invoice.invoiceNumber}", margin + 20f, y + 63f, makePaint(13f, false, onPrimaryColor))
                    canvas.drawText("TOTAL", pageWidth - margin - 120f, y + 34f, makePaint(10f, true, onPrimaryColor))
                    drawRight(money(invoice, invoice.totalMinor), pageWidth - margin - 18f, y + 64f, 18f, true, onPrimaryColor)
                    y += 120f
                }
                PdfTemplateLayout.BOXED_META -> {
                    canvas.drawText("INVOICE", margin, y + 38f, makePaint(28f, true, primaryColor))
                    canvas.drawText(invoice.organizationName.ifBlank { "Business" }, margin, y + 64f, makePaint(12f, false, mutedColor))
                    canvas.drawRoundRect(RectF(pageWidth - margin - 150f, y + 6f, pageWidth - margin, y + 70f), 10f, 10f, makePaint(color = templateStyle.tableHeaderColor))
                    drawRight("# ${invoice.invoiceNumber}", pageWidth - margin - 14f, y + 33f, 12.5f, true, primaryColor)
                    drawRight(invoice.date, pageWidth - margin - 14f, y + 55f, 11.5f, false, mutedColor)
                    y += 96f
                }
                PdfTemplateLayout.MINIMAL -> {
                    drawHeaderLogo(margin, y, 58)
                    drawRight("# ${invoice.invoiceNumber}", pageWidth - margin, y + 20f, 12f, false, mutedColor)
                    canvas.drawText("Invoice", margin, y + 76f, makePaint(30f, false, primaryDark))
                    canvas.drawLine(margin, y + 94f, pageWidth - margin, y + 94f, makePaint(color = primaryColor).apply { strokeWidth = 1.4f })
                    y += 118f
                }
                PdfTemplateLayout.SPLIT_BAND -> {
                    val split = margin + contentWidth / 2f
                    canvas.drawRoundRect(RectF(margin, y, split, y + 92f), 12f, 12f, makePaint(color = primaryColor))
                    canvas.drawRect(split - 12f, y, split, y + 92f, makePaint(color = primaryColor))
                    canvas.drawRoundRect(RectF(split, y, pageWidth - margin, y + 92f), 12f, 12f, makePaint(color = templateStyle.tableHeaderColor))
                    canvas.drawRect(split, y, split + 12f, y + 92f, makePaint(color = templateStyle.tableHeaderColor))
                    canvas.drawText("INVOICE", margin + 18f, y + 38f, makePaint(24f, true, onPrimaryColor))
                    canvas.drawText("# ${invoice.invoiceNumber}", margin + 18f, y + 64f, makePaint(13f, false, onPrimaryColor))
                    drawHeaderLogo(pageWidth - margin - 82f, y + 15f)
                    y += 116f
                }
                PdfTemplateLayout.STUDIO -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 108f), 18f, 18f, makePaint(color = templateStyle.tableHeaderColor))
                    drawHeaderLogo(margin + 18f, y + 15f, 64)
                    canvas.drawRoundRect(RectF(pageWidth - margin - 120f, y + 18f, pageWidth - margin - 18f, y + 48f), 15f, 15f, makePaint(color = primaryColor))
                    drawCentered(canvas, "# ${invoice.invoiceNumber}", pageWidth - margin - 69f, y + 38f, makePaint(11.5f, true, onPrimaryColor))
                    canvas.drawText("INVOICE", margin + 18f, y + 84f, makePaint(28f, true, primaryColor))
                    y += 132f
                }
                PdfTemplateLayout.CORPORATE -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 100f), 8f, 8f, makePaint(color = primaryColor))
                    canvas.drawText(invoice.organizationName.ifBlank { "Business" }, margin + 18f, y + 30f, makePaint(12f, true, onPrimaryColor))
                    canvas.drawText("INVOICE", margin + 18f, y + 63f, makePaint(27f, true, onPrimaryColor))
                    drawHeaderLogo(pageWidth - margin - 82f, y + 18f)
                    canvas.drawRect(margin, y + 74f, pageWidth - margin, y + 100f, makePaint(color = templateStyle.tableHeaderColor))
                    canvas.drawText("# ${invoice.invoiceNumber}  |  ${invoice.date}", margin + 18f, y + 92f, makePaint(11.5f, true, mutedColor))
                    y += 124f
                }
                PdfTemplateLayout.CLASSIC -> {
                    canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 92f), 12f, 12f, makePaint(color = primaryColor))
                    canvas.drawText("INVOICE", margin + 20f, y + 38f, makePaint(27f, true, onPrimaryColor))
                    canvas.drawText("# ${invoice.invoiceNumber}", margin + 20f, y + 66f, makePaint(13.5f, false, onPrimaryColor))
                    drawHeaderLogo(pageWidth - margin - 82f, y + 15f)
                    y += 116f
                }
            }

            val columnGap = 18f
            val columnWidth = (contentWidth - columnGap) / 2f
            val partyTop = y

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
            val chipWidth = (contentWidth - 16f) / 2f
            detailChip("Issue Date", invoice.date, margin, detailTop, chipWidth)
            detailChip("Due Date", invoice.dueDate, margin + chipWidth + 16f, detailTop, chipWidth)
            y = detailTop + 70f
            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.deliveryState.isNotBlank()) {
                y = labeledLine("Delivery State", invoice.deliveryState, margin, y, contentWidth)
                y += 10f
            }

            fun drawItemsHeader() {
                newPageIfNeeded(48f)
                canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 34f), 8f, 8f, makePaint(color = templateStyle.tableHeaderColor))
                canvas.drawText("ITEMS", margin + 12f, y + 22f, makePaint(11f, true, mutedColor))
                y += 42f
            }
            drawItemsHeader()
            invoice.items.forEachIndexed { index, item ->
                val description = if (invoice.showItemDescription && item.description.isNotBlank()) {
                    "${item.name}\n${item.description}"
                } else {
                    item.name
                }
                val descriptionLines = wrap(description, makePaint(13f, false), contentWidth - 28f).take(3)
                val rowHeight = 88f + (descriptionLines.size - 1).coerceAtLeast(0) * 16f
                newPageIfNeeded(rowHeight + 12f)
                val rowTop = y
                val rowColor = if (index % 2 == 0) Color.WHITE else alternatingRowColor
                canvas.drawRoundRect(RectF(margin, rowTop, pageWidth - margin, rowTop + rowHeight), 8f, 8f, makePaint(color = rowColor))
                var rowY = rowTop + 22f
                descriptionLines.forEachIndexed { lineIndex, line ->
                    canvas.drawText(line, margin + 12f, rowY, makePaint(13f, lineIndex == 0, primaryDark))
                    rowY += 16f
                }
                canvas.drawLine(margin + 12f, rowY + 2f, pageWidth - margin - 12f, rowY + 2f, makePaint(color = lineColor).apply { strokeWidth = 1f })
                val amountTop = rowY + 22f
                val qtyX = margin + 12f
                val priceRight = margin + contentWidth * 0.68f
                val totalRight = pageWidth - margin - 12f
                canvas.drawText("Qty", qtyX, amountTop, makePaint(9.5f, true, mutedColor))
                canvas.drawText(item.quantity.toString(), qtyX, amountTop + 17f, makePaint(12f, true, bodyColor))
                drawRight("Price", priceRight, amountTop, 9.5f, true, mutedColor)
                drawRight(money(invoice, item.unitPriceMinor), priceRight, amountTop + 17f, 12f, true, mutedColor)
                drawRight("Total", totalRight, amountTop, 9.5f, true, mutedColor)
                drawRight(money(invoice, item.totalMinor), totalRight, amountTop + 17f, 13f, true, primaryDark)
                y += rowHeight + 8f
            }
            y += 8f

            canvas.drawLine(margin, y, pageWidth - margin, y, makePaint(color = lineColor).apply { strokeWidth = 1f })
            y += 24f
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
                when (invoice.taxOption) {
                    TaxOption.CGST_SGST -> {
                        val cgst = invoice.taxAmountMinor / 2
                        val sgst = invoice.taxAmountMinor - cgst
                        summaryRow("CGST (${invoice.taxRatePercent / 2}%)", money(invoice, cgst))
                        summaryRow("SGST (${invoice.taxRatePercent / 2}%)", money(invoice, sgst))
                    }
                    TaxOption.IGST -> summaryRow("IGST (${invoice.taxRatePercent}%)", money(invoice, invoice.taxAmountMinor))
                    TaxOption.NON_TAXABLE -> Unit
                }
            }
            y += 4f
            canvas.drawLine(margin + 250f, y, pageWidth - margin, y, makePaint(color = lineColor).apply { strokeWidth = 1.4f })
            y += 22f
            summaryRow("Grand Total", money(invoice, invoice.totalMinor), bold = true, color = accentColor, size = 16f)
            if (invoice.roundOffMinor != 0L) {
                summaryRow("Round Off", money(invoice, invoice.roundOffMinor))
            }
            y += 8f
            canvas.drawText("AMOUNT IN WORDS", margin, y, makePaint(10.5f, true, mutedColor))
            y += 20f
            text(amountInWords(invoice.totalMinor, invoice.currencyCode, invoice.decimalPlaces), size = 13.5f, bold = true, color = primaryDark)

            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.paymentDetails?.paymentMethod != PaymentMethod.NONE) {
                invoice.paymentDetails?.let { payment ->
                    section("Payment Details")
                    y = labeledLine("Method", payment.paymentMethod.name, margin, y, contentWidth)
                    y = labeledLine("Account Owner", payment.accountOwnerName.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("Account", payment.accountNumber.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("Bank", payment.bankName.orEmpty(), margin, y, contentWidth)
                    y = labeledLine("UPI", payment.upiId.orEmpty(), margin, y, contentWidth)
                    y += 8f
                }
            }

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

    private fun drawCentered(canvas: Canvas, value: String, centerX: Float, baseline: Float, paint: Paint) {
        canvas.drawText(value, centerX - paint.measureText(value) / 2f, baseline, paint)
    }

    private enum class PdfTemplateLayout {
        CLASSIC,
        CENTERED,
        SIDE_RAIL,
        TOP_STRIPE,
        TOTAL_HERO,
        BOXED_META,
        MINIMAL,
        SPLIT_BAND,
        LEDGER,
        STUDIO,
        CORPORATE
    }

    private fun pdfTemplateLayout(templateId: String): PdfTemplateLayout {
        val normalized = when (templateId) {
            "elegant_blue" -> "blue_split"
            "royal_purple" -> "royal_plum"
            else -> templateId
        }
        return when (normalized) {
            "center_mark" -> PdfTemplateLayout.CENTERED
            "left_rail" -> PdfTemplateLayout.SIDE_RAIL
            "stripe_classic" -> PdfTemplateLayout.TOP_STRIPE
            "total_focus" -> PdfTemplateLayout.TOTAL_HERO
            "boxed_meta" -> PdfTemplateLayout.BOXED_META
            "minimal_letter" -> PdfTemplateLayout.MINIMAL
            "split_brand" -> PdfTemplateLayout.SPLIT_BAND
            "ledger_pro" -> PdfTemplateLayout.LEDGER
            "studio_card" -> PdfTemplateLayout.STUDIO
            "corporate_panel" -> PdfTemplateLayout.CORPORATE
            else -> PdfTemplateLayout.CLASSIC
        }
    }

    private data class PdfTemplateStyle(
        val headerColor: Int,
        val onHeaderColor: Int,
        val accentColor: Int,
        val tableHeaderColor: Int
    )

    private fun pdfTemplateStyle(templateId: String): PdfTemplateStyle {
        val normalized = when (templateId) {
            "elegant_blue" -> "blue_split"
            "royal_purple" -> "royal_plum"
            else -> templateId
        }
        return when (normalized) {
            "coral_breeze" -> PdfTemplateStyle(
                headerColor = Color.rgb(232, 93, 93),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(232, 93, 93),
                tableHeaderColor = Color.rgb(207, 250, 241)
            )
            "crimson_edge" -> PdfTemplateStyle(
                headerColor = Color.rgb(231, 76, 60),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(231, 76, 60),
                tableHeaderColor = Color.rgb(255, 218, 214)
            )
            "ruby_luxe" -> PdfTemplateStyle(
                headerColor = Color.rgb(181, 18, 62),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(216, 27, 96),
                tableHeaderColor = Color.rgb(255, 214, 228)
            )
            "violet_gradient" -> PdfTemplateStyle(
                headerColor = Color.rgb(109, 40, 217),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(124, 58, 237),
                tableHeaderColor = Color.rgb(237, 233, 254)
            )
            "watercolor_gradient" -> PdfTemplateStyle(
                headerColor = Color.rgb(225, 112, 85),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(225, 112, 85),
                tableHeaderColor = Color.rgb(255, 228, 184)
            )
            "blue_split" -> PdfTemplateStyle(
                headerColor = Color.rgb(29, 78, 216),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(37, 99, 235),
                tableHeaderColor = Color.rgb(219, 234, 254)
            )
            "inferno_line" -> PdfTemplateStyle(
                headerColor = Color.rgb(194, 65, 12),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(249, 115, 22),
                tableHeaderColor = Color.rgb(255, 237, 213)
            )
            "poppins_breeze" -> PdfTemplateStyle(
                headerColor = Color.rgb(2, 132, 199),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(56, 189, 248),
                tableHeaderColor = Color.rgb(224, 242, 254)
            )
            "bluecrest" -> PdfTemplateStyle(
                headerColor = Color.rgb(23, 59, 99),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(30, 90, 167),
                tableHeaderColor = Color.rgb(215, 230, 245)
            )
            "elegant_gold" -> PdfTemplateStyle(
                headerColor = Color.rgb(31, 58, 95),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(212, 160, 23),
                tableHeaderColor = Color.rgb(255, 232, 163)
            )
            "modern_circle" -> PdfTemplateStyle(
                headerColor = Color.rgb(67, 56, 202),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(20, 184, 166),
                tableHeaderColor = Color.rgb(204, 251, 241)
            )
            "royal_plum" -> PdfTemplateStyle(
                headerColor = Color.rgb(91, 33, 182),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(139, 92, 246),
                tableHeaderColor = Color.rgb(237, 233, 254)
            )
            "teal_flow" -> PdfTemplateStyle(
                headerColor = Color.rgb(0, 105, 92),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(0, 150, 136),
                tableHeaderColor = Color.rgb(178, 223, 219)
            )
            "center_mark" -> PdfTemplateStyle(
                headerColor = Color.rgb(30, 64, 175),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(37, 99, 235),
                tableHeaderColor = Color.rgb(219, 234, 254)
            )
            "left_rail" -> PdfTemplateStyle(
                headerColor = Color.rgb(14, 116, 144),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(8, 145, 178),
                tableHeaderColor = Color.rgb(207, 250, 254)
            )
            "stripe_classic" -> PdfTemplateStyle(
                headerColor = Color.rgb(161, 98, 7),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(234, 179, 8),
                tableHeaderColor = Color.rgb(254, 243, 199)
            )
            "total_focus" -> PdfTemplateStyle(
                headerColor = Color.rgb(22, 101, 52),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(22, 163, 74),
                tableHeaderColor = Color.rgb(220, 252, 231)
            )
            "boxed_meta" -> PdfTemplateStyle(
                headerColor = Color.rgb(154, 52, 18),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(124, 45, 18),
                tableHeaderColor = Color.rgb(255, 237, 213)
            )
            "minimal_letter" -> PdfTemplateStyle(
                headerColor = Color.rgb(51, 65, 85),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(51, 65, 85),
                tableHeaderColor = Color.rgb(226, 232, 240)
            )
            "split_brand" -> PdfTemplateStyle(
                headerColor = Color.rgb(190, 24, 93),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(219, 39, 119),
                tableHeaderColor = Color.rgb(252, 231, 243)
            )
            "ledger_pro" -> PdfTemplateStyle(
                headerColor = Color.rgb(17, 94, 89),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(15, 118, 110),
                tableHeaderColor = Color.rgb(204, 251, 241)
            )
            "studio_card" -> PdfTemplateStyle(
                headerColor = Color.rgb(126, 34, 206),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(147, 51, 234),
                tableHeaderColor = Color.rgb(243, 232, 255)
            )
            "corporate_panel" -> PdfTemplateStyle(
                headerColor = Color.rgb(23, 37, 84),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(30, 58, 138),
                tableHeaderColor = Color.rgb(219, 234, 254)
            )
            "classic_business" -> PdfTemplateStyle(
                headerColor = Color.rgb(31, 41, 55),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(55, 65, 81),
                tableHeaderColor = Color.rgb(229, 231, 235)
            )
            "minimal_black" -> PdfTemplateStyle(
                headerColor = Color.rgb(17, 24, 39),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(17, 24, 39),
                tableHeaderColor = Color.rgb(243, 244, 246)
            )
            "soft_green" -> PdfTemplateStyle(
                headerColor = Color.rgb(4, 120, 87),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(5, 150, 105),
                tableHeaderColor = Color.rgb(209, 250, 229)
            )
            "premium_gold" -> PdfTemplateStyle(
                headerColor = Color.rgb(120, 53, 15),
                onHeaderColor = Color.rgb(255, 251, 235),
                accentColor = Color.rgb(180, 83, 9),
                tableHeaderColor = Color.rgb(254, 243, 199)
            )
            "corporate_slate" -> PdfTemplateStyle(
                headerColor = Color.rgb(51, 65, 85),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(71, 85, 105),
                tableHeaderColor = Color.rgb(226, 232, 240)
            )
            "creative_coral" -> PdfTemplateStyle(
                headerColor = Color.rgb(225, 29, 72),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(244, 63, 94),
                tableHeaderColor = Color.rgb(255, 228, 230)
            )
            "clean_ledger" -> PdfTemplateStyle(
                headerColor = Color.rgb(15, 118, 110),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(13, 148, 136),
                tableHeaderColor = Color.rgb(204, 251, 241)
            )
            else -> PdfTemplateStyle(
                headerColor = Color.rgb(15, 143, 131),
                onHeaderColor = Color.WHITE,
                accentColor = Color.rgb(15, 143, 131),
                tableHeaderColor = Color.rgb(204, 251, 241)
            )
        }
    }

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
