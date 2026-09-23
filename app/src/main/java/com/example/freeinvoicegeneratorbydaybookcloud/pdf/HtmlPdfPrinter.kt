package com.example.freeinvoicegeneratorbydaybookcloud.pdf

import android.content.Context
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PdfCallbacks
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal object HtmlPdfPrinter {
    suspend fun write(context: Context, html: String, baseUrl: String, file: File) {
        withContext(Dispatchers.Main) {
            val webView = WebView(context)
            val cancellation = CancellationSignal()
            var adapter: PrintDocumentAdapter? = null
            var destination: ParcelFileDescriptor? = null
            try {
                val completed = withTimeoutOrNull(30_000L) {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        fun fail(message: String) {
                            if (continuation.isActive) continuation.resumeWithException(IOException(message))
                        }
                        var printing = false
                        webView.settings.javaScriptEnabled = false
                        webView.settings.textZoom = 100
                        webView.settings.blockNetworkLoads = true
                        webView.webViewClient = object : WebViewClient() {
                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame) fail("Unable to load invoice: ${error.description}")
                            }

                            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                                fail("Invoice renderer stopped. Please try again.")
                                return true
                            }

                            override fun onPageFinished(view: WebView, url: String?) {
                                if (printing || !continuation.isActive) return
                                printing = true
                                try {
                                    val printAdapter = view.createPrintDocumentAdapter(file.name)
                                    adapter = printAdapter
                                    val attributes = PrintAttributes.Builder()
                                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                        .setResolution(PrintAttributes.Resolution("pdf", "PDF", 300, 300))
                                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                                        .build()
                                    printAdapter.onStart()
                                    printAdapter.onLayout(null, attributes, cancellation,
                                        object : PdfCallbacks.Layout() {
                                            override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                                                if (!continuation.isActive) return
                                                try {
                                                    val descriptor = ParcelFileDescriptor.open(file,
                                                        ParcelFileDescriptor.MODE_CREATE or
                                                            ParcelFileDescriptor.MODE_TRUNCATE or
                                                            ParcelFileDescriptor.MODE_READ_WRITE)
                                                    destination = descriptor
                                                    printAdapter.onWrite(arrayOf(PageRange.ALL_PAGES), descriptor, cancellation,
                                                        object : PdfCallbacks.Write() {
                                                            override fun onWriteFinished(pages: Array<out PageRange>) {
                                                                if (!continuation.isActive) return
                                                                if (pages.isEmpty() || file.length() == 0L) {
                                                                    fail("Invoice PDF is empty.")
                                                                } else {
                                                                    continuation.resume(Unit)
                                                                }
                                                            }
                                                            override fun onWriteFailed(error: CharSequence?) {
                                                                fail(error?.toString() ?: "Unable to write invoice PDF.")
                                                            }
                                                            override fun onWriteCancelled() { fail("Invoice PDF was cancelled.") }
                                                        })
                                                } catch (error: Exception) {
                                                    fail(error.message ?: "Unable to write invoice PDF.")
                                                }
                                            }
                                            override fun onLayoutFailed(error: CharSequence?) {
                                                fail(error?.toString() ?: "Unable to lay out invoice PDF.")
                                            }
                                            override fun onLayoutCancelled() { fail("Invoice layout was cancelled.") }
                                        }, null)
                                } catch (error: Exception) {
                                    fail(error.message ?: "Unable to print invoice.")
                                }
                            }
                        }
                        webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
                    }
                    true
                }
                if (completed != true) throw IOException("Invoice PDF took too long. Please try again.")
            } finally {
                // Cancellation resumes this finally block on Main, like all WebView calls.
                cancellation.cancel()
                try {
                    adapter?.onFinish()
                } finally {
                    try {
                        destination?.close()
                    } finally {
                        webView.stopLoading()
                        webView.destroy()
                    }
                }
            }
        }
    }
}
