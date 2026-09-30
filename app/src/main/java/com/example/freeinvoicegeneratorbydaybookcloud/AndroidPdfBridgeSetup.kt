package com.example.freeinvoicegeneratorbydaybookcloud

import android.content.Context
import com.example.freeinvoicegeneratorbydaybookcloud.pdf.HtmlPdfPrinter
import com.example.freeinvoicegeneratorbydaybookcloud.platform.AndroidPdfBridge
import java.io.File

fun registerAndroidPdfBridge(context: Context) {
    val appContext = context.applicationContext
    AndroidPdfBridge.writeHtml = { html, baseUrl, file ->
        HtmlPdfPrinter.write(appContext, html, baseUrl, file)
    }
}
