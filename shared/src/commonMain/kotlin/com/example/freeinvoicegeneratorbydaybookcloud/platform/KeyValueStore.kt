package com.example.freeinvoicegeneratorbydaybookcloud.platform

/** Cross-platform key-value persistence (SharedPreferences on Android, UserDefaults on iOS). */
interface KeyValueStore {
    fun getString(key: String, default: String = ""): String
    fun putString(key: String, value: String)
    fun getInt(key: String, default: Int = 0): Int
    fun putInt(key: String, value: Int)
    fun getStringOrNull(key: String): String?
    fun putStringOrNull(key: String, value: String?)
}

expect fun createKeyValueStore(): KeyValueStore
