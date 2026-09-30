package com.example.freeinvoicegeneratorbydaybookcloud.platform

import android.content.Context

private class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("daybook_settings", Context.MODE_PRIVATE)

    override fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun getStringOrNull(key: String): String? = prefs.getString(key, null)

    override fun putStringOrNull(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }
}

actual fun createKeyValueStore(): KeyValueStore =
    AndroidKeyValueStore(requireAppContext())
