package com.example.freeinvoicegeneratorbydaybookcloud.platform

/** Called once at app startup. On Android pass the application [Context]. */
expect fun initializePlatform(context: Any? = null)

expect fun createHttpClient(): io.ktor.client.HttpClient
