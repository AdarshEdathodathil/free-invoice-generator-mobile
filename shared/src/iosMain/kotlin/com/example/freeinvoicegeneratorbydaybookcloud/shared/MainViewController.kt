package com.example.freeinvoicegeneratorbydaybookcloud.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.example.freeinvoicegeneratorbydaybookcloud.di.AppContainer
import com.example.freeinvoicegeneratorbydaybookcloud.platform.initializePlatform
import platform.UIKit.UIViewController

private val appContainer: AppContainer by lazy {
    initializePlatform(null)
    AppContainer()
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    SharedApp(appContainer)
}
