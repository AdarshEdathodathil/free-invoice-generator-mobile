package com.example.freeinvoicegeneratorbydaybookcloud.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.freeinvoicegeneratorbydaybookcloud.R
import com.example.freeinvoicegeneratorbydaybookcloud.pdf.InvoicePdfGenerator

object InvoiceDownloadNotifier {
    const val CHANNEL_ID = "invoice_downloads"

    fun canShowNotification(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    fun showDownloaded(context: Context, uri: Uri) {
        if (!canShowNotification(context)) return

        createChannel(context)

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, InvoicePdfGenerator.PDF_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            uri.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Invoice downloaded")
            .setContentText("Tap to open the saved PDF invoice.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(uri.hashCode(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Invoice downloads",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications shown after invoice PDFs are downloaded."
        }
        notificationManager.createNotificationChannel(channel)
    }
}
