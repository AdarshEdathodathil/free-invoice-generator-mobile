package com.example.freeinvoicegeneratorbydaybookcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.ui.navigation.DaybookNavGraph
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.FreeInvoiceGeneratorByDaybookCloudTheme
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.SettingsViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
}
