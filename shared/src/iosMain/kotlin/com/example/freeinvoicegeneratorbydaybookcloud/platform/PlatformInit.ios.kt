package com.example.freeinvoicegeneratorbydaybookcloud.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun initializePlatform(context: Any?) = Unit

actual fun createHttpClient(): HttpClient = HttpClient(Darwin)
