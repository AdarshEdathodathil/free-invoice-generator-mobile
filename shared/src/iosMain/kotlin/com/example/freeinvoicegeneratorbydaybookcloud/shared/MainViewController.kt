package com.example.freeinvoicegeneratorbydaybookcloud.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point called by the SwiftUI host in iosApp. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    SharedApp()
}
