package com.example.freeinvoicegeneratorbydaybookcloud.shared

actual fun platformName(): String = "iOS"

actual fun currentTimeMillis(): Long = (platform.Foundation.NSDate().timeIntervalSince1970 * 1_000).toLong()
