package com.example.freeinvoicegeneratorbydaybookcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.freeinvoicegeneratorbydaybookcloud.di.AppContainer
import com.example.freeinvoicegeneratorbydaybookcloud.platform.initializePlatform
import com.example.freeinvoicegeneratorbydaybookcloud.shared.SharedApp

class MainActivity : ComponentActivity() {

    private val appContainer by lazy {
        initializePlatform(applicationContext)
        AppContainer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SharedApp(appContainer)
        }
    }
}
