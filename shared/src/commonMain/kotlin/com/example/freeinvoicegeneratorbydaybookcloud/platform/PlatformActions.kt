package com.example.freeinvoicegeneratorbydaybookcloud.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap

interface PlatformActions {
    fun showToast(message: String)
    fun exitApplication()
    fun decodeLogo(path: String?): ImageBitmap?
    fun logoToBase64DataUri(path: String?): String
    suspend fun createSharePdf(html: String, baseUrl: String, fileName: String): Result<String>
    suspend fun savePdfToDownloads(html: String, baseUrl: String, fileName: String): Result<String>
    fun sharePdfFile(filePath: String)
    fun onDownloadSuccess(filePathOrUri: String)
    fun onDownloadError()
    fun canShowDownloadNotification(): Boolean
    fun requestDownloadNotificationPermission(onResult: (Boolean) -> Unit)
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> {
    error("PlatformActions not provided")
}

@Composable
expect fun HtmlWebView(html: String, baseUrl: String, modifier: androidx.compose.ui.Modifier)

@Composable
expect fun rememberImagePicker(onPicked: (String?) -> Unit): () -> Unit

expect fun configurePlatformActions(): PlatformActions
