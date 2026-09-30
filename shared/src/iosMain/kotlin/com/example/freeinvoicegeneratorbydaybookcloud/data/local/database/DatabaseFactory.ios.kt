@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.freeinvoicegeneratorbydaybookcloud.data.local.database

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createAppDatabase(): AppDatabase {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbPath = requireNotNull(documentDirectory?.path) + "/daybook_cloud.db"
    return buildDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath))
}
