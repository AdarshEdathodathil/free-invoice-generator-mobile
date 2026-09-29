package com.example.freeinvoicegeneratorbydaybookcloud.shared

expect fun platformName(): String

/** Current epoch time supplied by the platform, without exposing JVM APIs to common code. */
expect fun currentTimeMillis(): Long
