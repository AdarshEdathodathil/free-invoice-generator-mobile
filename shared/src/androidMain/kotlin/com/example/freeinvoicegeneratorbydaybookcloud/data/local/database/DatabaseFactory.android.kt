package com.example.freeinvoicegeneratorbydaybookcloud.data.local.database

import androidx.room.Room
import com.example.freeinvoicegeneratorbydaybookcloud.platform.requireAppContext

actual fun createAppDatabase(): AppDatabase {
    val context = requireAppContext()
    return buildDatabase(
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = context.getDatabasePath("daybook_cloud.db").absolutePath
        )
    )
}
