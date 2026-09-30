package com.example.freeinvoicegeneratorbydaybookcloud.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.BackgroundDark
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.BackgroundLight
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OnPrimary
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OnPrimaryContainer
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OnSurfaceLight
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OnSurfaceVariant
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OnSurfaceDark
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.OutlineLight
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.Primary
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.PrimaryContainer
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.PrimaryDark
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceContainerDefault
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceContainerHigh
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceContainerHighest
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceContainerLow
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceContainerLowest
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceLight
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceDark
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceVariantLight
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.SurfaceVariantDark
import com.example.freeinvoicegeneratorbydaybookcloud.ui.theme.Typography

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = PrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = OnSurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainerDefault,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = OutlineLight,
    outlineVariant = SurfaceContainerHigh,
    error = Color(0xFFB91C1C),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004F46),
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF21262D),
    onSecondaryContainer = Color(0xFFE6EDF3),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF8B949E),
    surfaceContainerLowest = Color(0xFF090E14),
    surfaceContainerLow = Color(0xFF0D1117),
    surfaceContainer = Color(0xFF161B22),
    surfaceContainerHigh = Color(0xFF21262D),
    surfaceContainerHighest = Color(0xFF30363D),
    outline = Color(0xFF3D444D),
    outlineVariant = Color(0xFF21262D),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690000)
)

@Composable
fun FreeInvoiceGeneratorByDaybookCloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
