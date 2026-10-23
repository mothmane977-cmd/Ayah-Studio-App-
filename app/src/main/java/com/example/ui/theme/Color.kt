package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// "AYAH STUDIO" — MIDNIGHT AURORA & GLASSMORPHISM PALETTE
// ============================================================================

// 1. Midnight Aurora Background Gradient (#0B0F19 to #1A1B2F)
val MidnightBgStart = Color(0xFF0B0F19)
val MidnightBgEnd = Color(0xFF1A1B2F)
val MidnightDeepSurface = Color(0xFF121626)

// 2. Aurora Glowing Orbs
val AuroraPurple = Color(0xFF7B2CBF)
val AuroraTeal = Color(0xFF00F5D4)
val AuroraMagenta = Color(0xFFFF006E)

// 3. Glassmorphism Card Tokens
val GlassCardFill = Color(0x0DFFFFFF)           // Colors.white.withOpacity(0.05)
val GlassCardElevatedFill = Color(0x17FFFFFF)   // Colors.white.withOpacity(0.09)
val GlassCardBorder = Color(0x1AFFFFFF)         // Colors.white.withOpacity(0.10)
val GlassCardHighlightBorder = Color(0x38FFFFFF)// Subtle specular top edge
val GlassWhiteGlow = Color(0x24FFFFFF)

// 4. Typography Tokens
val TextPureWhite = Color(0xFFFFFFFF)           // #FFFFFF Pure White for headings
val TextSoftGray = Color(0xFFA0AAB2)            // #A0AAB2 Soft Gray for subtitles

// 5. Vibrant Gradient Brushes
val AuroraButtonGradient = Brush.horizontalGradient(
    colors = listOf(AuroraPurple, AuroraTeal)
)

val AuroraAccentGradient = Brush.linearGradient(
    colors = listOf(AuroraPurple, AuroraMagenta, AuroraTeal)
)

val MidnightBackgroundGradient = Brush.verticalGradient(
    colors = listOf(MidnightBgStart, MidnightBgEnd)
)

// Mushaf Mode accents
val MushafRecitedRed = Color(0xFFFF006E)
val MushafPaperCream = Color(0xFFFDFBF7)

// Semantic compatibility aliases across screens
val SereneBackground = MidnightBgStart
val SereneCardSurface = GlassCardFill
val SereneCardBorder = GlassCardBorder
val SereneSubtleBorder = GlassCardBorder
val SereneInputSurface = GlassCardElevatedFill
val PrimaryCalmTeal = AuroraTeal
val PrimaryCalmTealContainer = Color(0x2600F5D4)
val SecondarySoftGold = AuroraPurple
val SecondarySoftGoldContainer = Color(0x2E7B2CBF)
val TextDarkSlate = TextPureWhite
val TextMutedSlate = TextSoftGray
val TextOnPrimaryWhite = TextPureWhite
val InactiveStepGray = Color(0x33A0AAB2)

val BgDarkNavy = MidnightBgStart
val CardNavy = GlassCardFill
val CardElevatedNavy = GlassCardBorder
val PrimaryNeonTeal = AuroraTeal
val PrimaryNeonTealDim = Color(0x2600F5D4)
val TextHeadingWhite = TextPureWhite
val TextSubtitleGray = TextSoftGray
val BgVeryDarkGreen = MidnightBgStart
val CardEmeraldGreen = GlassCardFill
val CardElevatedEmerald = GlassCardBorder
val PrimaryGold = AuroraTeal
val PrimaryGoldDim = Color(0x2600F5D4)
val EmeraldButtonGreen = AuroraPurple
val InactiveDotGreen = Color(0x33A0AAB2)
