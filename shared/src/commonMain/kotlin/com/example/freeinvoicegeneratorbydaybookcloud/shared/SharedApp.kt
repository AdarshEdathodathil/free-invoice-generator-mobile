package com.example.freeinvoicegeneratorbydaybookcloud.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.freeinvoicegeneratorbydaybookcloud.shared.templates.TemplateScreen

/** iOS and Android Compose entry point for the incrementally shared UI. */
@Composable
fun SharedApp() {
    MaterialTheme {
        TemplateScreen()
    }
}
