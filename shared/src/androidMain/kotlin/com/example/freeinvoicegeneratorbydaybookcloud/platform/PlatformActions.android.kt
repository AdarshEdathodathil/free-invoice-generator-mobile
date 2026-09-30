package com.example.freeinvoicegeneratorbydaybookcloud.platform

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.freeinvoicegeneratorbydaybookcloud.util.InvoiceDownloadNotifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun configurePlatformActions(): PlatformActions = AndroidPlatformActions()

private class AndroidPlatformActions : PlatformActions {
    private val context get() = requireAppContext()

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun exitApplication() {
        (context as? android.app.Application)?.let { app ->
            app.registerActivityLifecycleCallbacks(object : android.app.Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) = Unit
                override fun onActivityStarted(activity: android.app.Activity) = Unit
                override fun onActivityResumed(activity: android.app.Activity) = Unit
                override fun onActivityPaused(activity: android.app.Activity) = Unit
                override fun onActivityStopped(activity: android.app.Activity) = Unit
                override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) = Unit
                override fun onActivityDestroyed(activity: android.app.Activity) = Unit
            })
        }
        val activity = context as? android.app.Activity
        activity?.finishAffinity()
    }

    override fun decodeLogo(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) return null
        return runCatching {
            val parsed = Uri.parse(path)
            val stream = when (parsed.scheme) {
                "content", "android.resource", "file" -> context.contentResolver.openInputStream(parsed)
                null, "" -> File(path).takeIf { it.exists() }?.inputStream()
                else -> null
            }
            stream?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        }.getOrNull()
    }

    override fun logoToBase64DataUri(path: String?): String {
        if (path.isNullOrBlank()) return ""
        return runCatching {
            val parsed = Uri.parse(path)
            val stream = when (parsed.scheme) {
                "content", "android.resource", "file" -> context.contentResolver.openInputStream(parsed)
                null, "" -> File(path).takeIf { it.exists() }?.inputStream()
                else -> null
            } ?: return ""
            val bitmap = stream.use { BitmapFactory.decodeStream(it) } ?: return ""
            val output = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
            "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray())
        }.getOrDefault("")
    }

    override suspend fun createSharePdf(html: String, baseUrl: String, fileName: String): Result<String> =
        runCatching {
            withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
                val file = File(directory, fileName)
                try {
                    val writer = AndroidPdfBridge.writeHtml
                        ?: error("AndroidPdfBridge is not initialized")
                    writer(html, baseUrl, file)
                    file.absolutePath
                } catch (error: Exception) {
                    file.delete()
                    throw error
                }
            }
        }

    override suspend fun savePdfToDownloads(html: String, baseUrl: String, fileName: String): Result<String> =
        runCatching {
            val tempPath = createSharePdf(html, baseUrl, fileName).getOrThrow()
            val tempFile = File(tempPath)
            withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(android.provider.MediaStore.Downloads.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                        put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: error("Unable to create Downloads entry.")
                    try {
                        resolver.openOutputStream(uri)?.use { output ->
                            tempFile.inputStream().use { input -> input.copyTo(output) }
                        } ?: error("Unable to open Downloads output stream.")
                        values.clear()
                        values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                        uri.toString()
                    } catch (error: Exception) {
                        resolver.delete(uri, null, null)
                        throw error
                    }
                } else {
                    val downloads = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                        ?: error("Downloads directory is unavailable.")
                    downloads.mkdirs()
                    val target = File(downloads, tempFile.name)
                    tempFile.copyTo(target, overwrite = true)
                    Uri.fromFile(target).toString()
                }
            }
        }

    override fun sharePdfFile(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share invoice").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun onDownloadSuccess(filePathOrUri: String) {
        showToast("Invoice downloaded successfully")
        if (canShowDownloadNotification()) {
            InvoiceDownloadNotifier.showDownloaded(context, Uri.parse(filePathOrUri))
        }
    }

    override fun onDownloadError() {
        showToast("Unable to download invoice")
    }

    override fun canShowDownloadNotification(): Boolean =
        InvoiceDownloadNotifier.canShowNotification(context)

    override fun requestDownloadNotificationPermission(onResult: (Boolean) -> Unit) {
        onResult(canShowDownloadNotification())
    }
}

@Composable
actual fun HtmlWebView(html: String, baseUrl: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = false
                webViewClient = WebViewClient()
                loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
        }
    )
}

@Composable
actual fun rememberImagePicker(onPicked: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                requireAppContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onPicked(uri.toString())
        }
    }
    return remember(launcher) { { launcher.launch(arrayOf("image/*")) } }
}
