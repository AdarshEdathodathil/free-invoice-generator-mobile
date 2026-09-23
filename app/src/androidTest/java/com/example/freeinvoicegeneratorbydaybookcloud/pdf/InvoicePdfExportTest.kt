package com.example.freeinvoicegeneratorbydaybookcloud.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Invoice
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceItem
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Organization
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.toUiModel
import com.example.freeinvoicegeneratorbydaybookcloud.util.LogoResolver
import java.io.File
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class InvoicePdfExportTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val generator = InvoicePdfGenerator(context, LogoResolver())

    @Test
    fun selectedHtmlTemplateColorsArePresentInExport() = runBlocking {
        for ((template, color) in listOf("elegant_gold" to "#1a237e", "coral_breeze" to "#ff6b6b")) {
            val file = generator.createSharePdf(invoice(), template).getOrThrow()
            try {
                withPdf(file) { renderer ->
                    assertTrue(renderer.pageCount > 0)
                    renderer.openPage(0).use { page ->
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        try {
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val expected = Color.parseColor(color)
                            var matchingPixels = 0
                            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                                val pixel = bitmap.getPixel(x, y)
                                if (abs(Color.red(pixel) - Color.red(expected)) < 12 &&
                                    abs(Color.green(pixel) - Color.green(expected)) < 12 &&
                                    abs(Color.blue(pixel) - Color.blue(expected)) < 12) matchingPixels++
                            }
                            assertTrue("$template must retain its HTML colors ($matchingPixels pixels)", matchingPixels > 100)
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            } finally {
                file.delete()
            }
        }
    }

    @Test
    fun modernGradientUsesNarrowPageBorders() = runBlocking {
        val file = generator.createSharePdf(invoice(2), "freelancer_modern_gradient").getOrThrow()
        try {
            withPdf(file) { renderer ->
                assertTrue("Short invoice should fit on one page", renderer.pageCount == 1)
                renderer.openPage(0).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    try {
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val artifacts = File(context.getExternalFilesDir(null), "pdf-test-artifacts").apply { mkdirs() }
                        File(artifacts, "modern-gradient.png").outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                        val headerPixels = (0 until bitmap.width).filter { x ->
                            val color = bitmap.getPixel(x, 35)
                            Color.red(color) in 40..60 && Color.green(color) in 55..80 &&
                                Color.blue(color) in 70..100
                        }
                        assertTrue("Header should extend to within 6 mm of both sides",
                            headerPixels.isNotEmpty() && headerPixels.first() <= 19 &&
                                headerPixels.last() >= bitmap.width - 20)
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun downloadContainsTheGeneratedHtmlPdf() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val testInvoice = invoice()
        val uri = generator.saveToDownloads(testInvoice, "elegant_gold").getOrThrow()
        val cached = File(context.cacheDir, "invoices/${generator.safeFileName(testInvoice.invoiceNumber)}")
        try {
            val downloaded = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
            assertTrue("Downloads must contain the same PDF", cached.readBytes().contentEquals(downloaded))
            context.contentResolver.openFileDescriptor(uri, "r")!!.use { descriptor ->
                PdfRenderer(descriptor).use { assertTrue(it.pageCount > 0) }
            }
        } finally {
            context.contentResolver.delete(uri, null, null)
            cached.delete()
        }
    }

    @Test
    fun longInvoiceExportsMultiplePages() = runBlocking {
        val file = generator.createSharePdf(invoice(90), "elegant_gold").getOrThrow()
        try {
            withPdf(file) { assertTrue("Long invoice should paginate", it.pageCount > 1) }
        } finally {
            file.delete()
        }
    }

    @Test
    fun cancellingFromBackgroundThreadAllowsNextExport() = runBlocking {
        val file = File(context.cacheDir, "cancelled-export-test.pdf")
        try {
            val task = withContext(Dispatchers.Main) {
                this@runBlocking.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
                    HtmlPdfPrinter.write(context, "<html><body>Cancelled</body></html>",
                        "file:///android_asset/", file)
                }
            }
            task.cancelAndJoin()
            val next = generator.createSharePdf(invoice(), "elegant_gold").getOrThrow()
            try {
                withPdf(next) { assertTrue(it.pageCount > 0) }
            } finally {
                next.delete()
            }
        } finally {
            file.delete()
        }
    }

    private fun invoice(itemCount: Int = 1) = Invoice(
        invoiceNumber = "PDF-EXPORT-TEST", organizationId = 0, customerId = 0,
        invoiceDate = 1_700_000_000_000L, dueDate = 1_701_000_000_000L,
        subtotalMinor = itemCount * 1000L, taxAmountMinor = 0, totalMinor = itemCount * 1000L,
        organization = Organization(name = "Export Test Company", address = "Test Street"),
        items = List(itemCount) { index ->
            InvoiceItem(name = "Service ${index + 1}", quantity = 1.0, unitPriceMinor = 1000,
                lineSubtotalMinor = 1000, taxAmountMinor = 0, totalMinor = 1000)
        }
    ).toUiModel()

    private fun withPdf(file: File, block: (PdfRenderer) -> Unit) {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use(block)
        }
    }
}
