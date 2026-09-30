@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.freeinvoicegeneratorbydaybookcloud.platform

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.WebKit.WKWebView
import platform.darwin.NSObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual fun configurePlatformActions(): PlatformActions = IosPlatformActions()

private class IosPlatformActions : PlatformActions {
    override fun showToast(message: String) {
        println("Daybook: $message")
    }

    override fun exitApplication() {
        // iOS apps generally should not force-quit themselves.
    }

    override fun decodeLogo(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) return null
        val filePath = path.removePrefix("file://")
        val data = NSData.dataWithContentsOfFile(filePath) ?: return null
        return runCatching {
            Image.makeFromEncoded(nsDataToByteArray(data)).toComposeImageBitmap()
        }.getOrNull()
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun logoToBase64DataUri(path: String?): String {
        if (path.isNullOrBlank()) return ""
        val filePath = path.removePrefix("file://")
        val data = NSData.dataWithContentsOfFile(filePath) ?: return ""
        val encoded = Base64.encode(nsDataToByteArray(data))
        return "data:image/png;base64,$encoded"
    }

    override suspend fun createSharePdf(html: String, baseUrl: String, fileName: String): Result<String> =
        writePdfFile(html, baseUrl, fileName)

    override suspend fun savePdfToDownloads(html: String, baseUrl: String, fileName: String): Result<String> =
        writePdfFile(html, baseUrl, fileName)

    override fun sharePdfFile(filePath: String) {
        val url = NSURL.fileURLWithPath(filePath)
        val controller = UIActivityViewController(listOf(url), null)
        topViewController()?.presentViewController(controller, true, null)
    }

    override fun onDownloadSuccess(filePathOrUri: String) {
        showToast("Invoice saved successfully")
    }

    override fun onDownloadError() {
        showToast("Unable to save invoice")
    }

    override fun canShowDownloadNotification(): Boolean = false

    override fun requestDownloadNotificationPermission(onResult: (Boolean) -> Unit) {
        onResult(false)
    }

    private suspend fun writePdfFile(html: String, baseUrl: String, fileName: String): Result<String> =
        withContext(Dispatchers.Default) {
            runCatching {
                val directory = NSTemporaryDirectory() + "invoices/"
                NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)
                val filePath = directory + fileName
                renderHtmlToPdfFile(html, baseUrl, filePath)
                filePath
            }
        }
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

@Composable
actual fun HtmlWebView(html: String, baseUrl: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier.fillMaxSize(),
        factory = {
            WKWebView().apply {
                loadHTMLString(html, baseURL = NSURL.URLWithString(baseUrl))
            }
        },
        update = { webView ->
            webView.loadHTMLString(html, baseURL = NSURL.URLWithString(baseUrl))
        }
    )
}

@Composable
actual fun rememberImagePicker(onPicked: (String?) -> Unit): () -> Unit {
    val pickerHost = remember { IosImagePickerHost(onPicked) }
    return remember(pickerHost) { { pickerHost.launch() } }
}

private class IosImagePickerHost(
    private val onPicked: (String?) -> Unit
) {
    private var delegate: NSObject? = null

    fun launch() {
        val presenter = topViewController() ?: run {
            onPicked(null)
            return
        }
        val picker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            allowsEditing = false
        }
        val pickerDelegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                val savedPath = image?.let { savePickedImage(it) }
                picker.dismissViewControllerAnimated(true, null)
                delegate = null
                onPicked(savedPath)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
                delegate = null
                onPicked(null)
            }
        }
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        presenter.presentViewController(picker, true, null)
    }

    private fun savePickedImage(image: UIImage): String? {
        val pngData = UIImagePNGRepresentation(image) ?: return null
        val directory = NSTemporaryDirectory() + "logos/"
        NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)
        val filePath = directory + "logo-${kotlin.random.Random.nextInt()}.png"
        return runCatching {
            writeBytesToFile(nsDataToByteArray(pngData), filePath)
            filePath
        }.getOrNull()
    }
}
