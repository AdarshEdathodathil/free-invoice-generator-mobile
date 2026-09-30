package com.example.freeinvoicegeneratorbydaybookcloud.platform

import java.io.File

object AndroidPdfBridge {
    var writeHtml: (suspend (html: String, baseUrl: String, file: File) -> Unit)? = null
}
