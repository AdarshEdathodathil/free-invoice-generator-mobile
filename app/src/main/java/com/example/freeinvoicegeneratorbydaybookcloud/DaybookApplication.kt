package com.example.freeinvoicegeneratorbydaybookcloud

import android.app.Application
import com.example.freeinvoicegeneratorbydaybookcloud.platform.initializePlatform

class DaybookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializePlatform(this)
        registerAndroidPdfBridge(this)
    }
}
