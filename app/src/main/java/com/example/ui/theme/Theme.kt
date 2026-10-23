package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AyahStudioMidnightAuroraColorScheme = darkColorScheme(
    primary = AuroraTeal,
    onPrimary = MidnightBgStart,
    primaryContainer = Color(0x337B2CBF),
    onPrimaryContainer = TextPureWhite,
    secondary = AuroraPurple,
    onSecondary = TextPureWhite,
    secondaryContainer = Color(0x337B2CBF),
    onSecondaryContainer = TextPureWhite,
    tertiary = AuroraMagenta,
    onTertiary = TextPureWhite,
    background = MidnightBgStart,
    onBackground = TextPureWhite,
    surface = MidnightDeepSurface,
    onSurface = TextPureWhite,
    surfaceVariant = GlassCardElevatedFill,
    onSurfaceVariant = TextSoftGray,
    outline = GlassCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AyahStudioMidnightAuroraColorScheme,
        typography = Typography,
        content = content
    )
}
