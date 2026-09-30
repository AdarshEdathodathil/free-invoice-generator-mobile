package com.example.freeinvoicegeneratorbydaybookcloud.platform

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

private lateinit var appContext: Context

internal fun requireAppContext(): Context {
    check(::appContext.isInitialized) { "Call initializePlatform(context) before using shared UI." }
    return appContext
}

actual fun initializePlatform(context: Any?) {
    appContext = (context as Context).applicationContext
}

actual fun createHttpClient(): HttpClient = HttpClient(Android)
