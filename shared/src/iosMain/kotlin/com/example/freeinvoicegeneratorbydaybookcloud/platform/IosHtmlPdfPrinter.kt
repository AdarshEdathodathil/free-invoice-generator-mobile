@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.example.freeinvoicegeneratorbydaybookcloud.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKPDFConfiguration
import platform.WebKit.WKWebView
import platform.darwin.NSObject
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun renderHtmlToPdfFile(html: String, baseUrl: String, filePath: String) {
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val webView = WKWebView()
            val delegate = object : NSObject(), WKNavigationDelegateProtocol {
                override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
                    webView.createPDFWithConfiguration(WKPDFConfiguration()) { data, error ->
                        if (error != null) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    IllegalStateException(error.localizedDescription?.toString() ?: "PDF generation failed")
                                )
                            }
                            return@createPDFWithConfiguration
                        }
                        if (data == null) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(IllegalStateException("PDF data is empty"))
                            }
                            return@createPDFWithConfiguration
                        }
                        runCatching { writeBytesToFile(nsDataToByteArray(data), filePath) }
                            .onSuccess {
                                if (continuation.isActive) continuation.resume(Unit)
                            }
                            .onFailure { failure ->
                                if (continuation.isActive) continuation.resumeWithException(failure)
                            }
                    }
                }
            }
            webView.navigationDelegate = delegate
            webView.loadHTMLString(html, baseURL = NSURL.URLWithString(baseUrl))
            continuation.invokeOnCancellation {
                webView.stopLoading()
                webView.navigationDelegate = null
            }
        }
    }
}

internal fun nsDataToByteArray(data: NSData): ByteArray {
    val length = data.length.toInt()
    if (length == 0) return ByteArray(0)
    val source = data.bytes ?: error("NSData bytes are unavailable")
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, length.convert())
    }
    return bytes
}

internal fun writeBytesToFile(bytes: ByteArray, filePath: String) {
    if (bytes.isEmpty()) error("File contents are empty")
    bytes.usePinned { pinned ->
        val file = fopen(filePath, "wb") ?: error("Cannot open $filePath for writing")
        try {
            val written = fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
            if (written.toLong() != bytes.size.toLong()) {
                error("Failed to write file to $filePath")
            }
        } finally {
            fclose(file)
        }
    }
}
