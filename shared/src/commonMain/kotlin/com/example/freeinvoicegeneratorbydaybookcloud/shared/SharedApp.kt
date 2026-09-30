package com.example.freeinvoicegeneratorbydaybookcloud.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freeinvoicegeneratorbydaybookcloud.di.AppContainer
import com.example.freeinvoicegeneratorbydaybookcloud.di.LocalAppContainer
import com.example.freeinvoicegeneratorbydaybookcloud.platform.LocalPlatformActions
import com.example.freeinvoicegeneratorbydaybookcloud.ui.navigation.DaybookNavGraph
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.FreeInvoiceGeneratorByDaybookCloudTheme
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.ThemeMode

@Composable
fun SharedApp(container: AppContainer) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppContainer provides container,
        LocalPlatformActions provides container.platformActions
    ) {
        val settingsViewModel = viewModel { container.settingsViewModel() }
        val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
        val systemInDark = isSystemInDarkTheme()
        val isDark = when (themeMode) {
            ThemeMode.SYSTEM -> systemInDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }

        FreeInvoiceGeneratorByDaybookCloudTheme(darkTheme = isDark) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DaybookNavGraph(settingsViewModel = settingsViewModel)
            }
        }
    }
}
