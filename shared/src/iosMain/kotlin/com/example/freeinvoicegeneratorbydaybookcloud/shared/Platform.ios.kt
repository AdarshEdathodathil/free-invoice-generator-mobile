package com.example.freeinvoicegeneratorbydaybookcloud.shared

import kotlinx.datetime.Clock

actual fun platformName(): String = "iOS"

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
