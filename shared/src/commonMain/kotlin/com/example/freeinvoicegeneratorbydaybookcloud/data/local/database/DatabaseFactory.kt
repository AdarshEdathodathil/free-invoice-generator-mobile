package com.example.freeinvoicegeneratorbydaybookcloud.data.local.database

import androidx.room.RoomDatabase

expect fun createAppDatabase(): AppDatabase

internal fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .addMigrations(MIGRATION_1_2)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.Default)
        .build()
