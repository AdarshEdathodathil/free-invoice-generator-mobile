package com.example.freeinvoicegeneratorbydaybookcloud.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import javax.inject.Inject

class LogoResolver @Inject constructor() {
    fun decode(context: Context, logoReference: String?): Bitmap? {
        if (logoReference.isNullOrBlank()) return null

        return runCatching {
            val parsed = Uri.parse(logoReference)
            val stream = when (parsed.scheme) {
                "content", "android.resource", "file" -> context.contentResolver.openInputStream(parsed)
                null, "" -> File(logoReference).takeIf { it.exists() }?.inputStream()
                else -> null
            }
            stream?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }
}
