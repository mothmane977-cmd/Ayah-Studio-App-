package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ArabicFontOption
import com.example.data.EnglishFontOption
import com.example.data.TextPosition
import com.example.data.VideoFilterConfig
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.AuroraButtonGradient
import com.example.ui.theme.AuroraMagenta
import com.example.ui.theme.AuroraPurple
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardElevatedFill
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.MushafPaperCream
import com.example.ui.theme.MushafRecitedRed
import com.example.ui.theme.RobotoFontFamily
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSoftGray
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * STEP 3 (Preview) — Midnight Aurora & Glassmorphism Aesthetic:
 * - Final 9:16 vertical preview canvas applying Pexels video background,
 *   real-time Brightness/Contrast/Saturation/Blur video filters,
 *   authentic RTL Uthmani script (Amiri Quran) with non-overlapping Eastern Arabic-Indic Ayah numbers ﴿١﴾,
 *   frosted glassmorphic verse card, and vibrant Purple-to-Cyan glowing "Generate" button.
 */
@Composable
fun LivePreviewAndExportScreen(
    state: QuranReelsUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onStepAyah: (Int) -> Unit,
    onOpenBackgrounds: () -> Unit,
    onOpenTextStyle: () -> Unit,
    onOpenFilters: () -> Unit,
    onStartMp4Export: () -> Unit,
    onResetExport: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "reel_bg_motion")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val activeAyah = state.authenticAyahs.getOrNull(state.activePreviewAyahIndex)
        ?: state.authenticAyahs.firstOrNull()

    val composeColorFilter = buildComposeColorFilter(state.videoFilterConfig)

    val selectedArabicFontFamily = when (state.textStyleConfig.arabicFont) {
        ArabicFontOption.AMIRI, ArabicFontOption.QPC_HAFS -> AmiriQuranFontFamily
        ArabicFontOption.SCHEHERAZADE -> AmiriFontFamily
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Design Tweak Pills (Frosted Glass)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDesignChip(
                icon = Icons.Default.Videocam,
                label = "Pexels Bg",
                onClick = onOpenBackgrounds,
                modifier = Modifier.weight(1f)
            )
            QuickDesignChip(
                icon = Icons.Default.FontDownload,
                label = "Text Style",
                onClick = onOpenTextStyle,
                modifier = Modifier.weight(1f)
            )
            QuickDesignChip(
                icon = Icons.Default.AutoFixHigh,
                label = "Filters",
                onClick = onOpenFilters,
                modifier = Modifier.weight(1f)
            )
        }

        // 9:16 Vertical Video Preview Canvas with Aurora Border Glow
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(9f / 16f)
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AuroraPurple.copy(alpha = 0.35f),
                                AuroraTeal.copy(alpha = 0.35f)
                            )
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(state.selectedBackground.primaryHex),
                            Color(state.selectedBackground.secondaryHex)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AuroraPurple.copy(alpha = 0.85f),
                            AuroraTeal.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            // Background Layer with real-time Brightness, Contrast, Saturation & Blur sliders!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (state.videoFilterConfig.blurRadius > 0.5f) {
                            Modifier.blur(state.videoFilterConfig.blurRadius.dp)
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (!state.selectedBackground.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = state.selectedBackground.thumbnailUrl,
                        contentDescription = state.selectedBackground.title,
                        contentScale = ContentScale.Crop,
                        colorFilter = composeColorFilter,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.14f),
                                        Color.Black.copy(alpha = 0.32f)
                                    )
                                )
                            )
                    )
                }

                // Subtle Aurora ambient motion orbs inside 9:16 canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    for (i in 0 until 14) {
                        val cx = w * (0.12f + (i * 0.065f) % 0.78f) + cos(animPhase + i) * 16f
                        val cy = ((h * ((i * 0.09f) % 1f)) - (animPhase * 35f)).mod(h)
                        val orbColor = when (i % 3) {
                            0 -> AuroraTeal
                            1 -> AuroraPurple
                            else -> AuroraMagenta
                        }
                        drawCircle(
                            color = orbColor.copy(alpha = 0.24f),
                            radius = (3f + (i % 4) * 2f).dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            // Surah Header Badge inside Reel (Frosted Glass Pill)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = "سورة ${state.selectedSurah.nameArabic}",
                        color = AuroraTeal,
                        fontFamily = selectedArabicFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "${state.selectedSurah.nameSimple} • Ayah ${activeAyah?.ayahNumber ?: state.startAyah} of ${state.selectedSurah.versesCount}",
                    color = TextPureWhite.copy(alpha = 0.90f),
                    fontSize = 11.sp
                )
            }

            if (state.isLoadingVerses) {
                CircularProgressIndicator(
                    color = AuroraTeal,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (activeAyah != null) {
                val selectedEnglishFontFamily = when (state.textStyleConfig.englishFont) {
                    EnglishFontOption.ROBOTO -> RobotoFontFamily
                    EnglishFontOption.SERIF -> FontFamily.Serif
                    EnglishFontOption.MONO -> FontFamily.Monospace
                }

                if (state.showMushafPage) {
                    // Mushaf Page Mode with RTL Amiri Quran & non-overlapping ﴿١﴾ markers
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.90f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MushafPaperCream.copy(alpha = 0.95f))
                            .border(1.5.dp, AuroraTeal, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "صفحة المصحف الشريف • Page ${activeAyah.pageNumber}",
                                color = Color(0xFF0B0F19),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val annotatedMushaf = buildAnnotatedString {
                                state.authenticAyahs.forEachIndexed { index, item ->
                                    val isRecitedNow = index == state.activePreviewAyahIndex
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (isRecitedNow) MushafRecitedRed else Color(0xFF0B0F19),
                                            fontWeight = if (isRecitedNow) FontWeight.Bold else FontWeight.Normal,
                                            background = if (isRecitedNow) MushafRecitedRed.copy(alpha = 0.14f) else Color.Transparent
                                        )
                                    ) {
                                        append("${item.formattedUthmaniWithNumber()} ")
                                    }
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = annotatedMushaf,
                                    fontFamily = selectedArabicFontFamily,
                                    fontSize = 21.sp,
                                    lineHeight = 38.sp,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(textDirection = TextDirection.Rtl)
                                )
                            }

                            if (state.textStyleConfig.showEnglishTranslation && activeAyah.translationText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = activeAyah.translationText,
                                    color = Color(0xFF1E293B),
                                    fontFamily = selectedEnglishFontFamily,
                                    fontSize = state.textStyleConfig.translationFontSizeSp.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (state.textStyleConfig.translationFontSizeSp * 1.35f).sp
                                )
                            }
                        }
                    }
                } else {
                    // Frosted Glassmorphism Verse Card in 9:16 Preview
                    val boxAlignment = when (state.textStyleConfig.textPosition) {
                        TextPosition.TOP -> Alignment.TopCenter
                        TextPosition.CENTER -> Alignment.Center
                        TextPosition.BOTTOM -> Alignment.BottomCenter
                    }
                    val parsedTextColor = runCatching {
                        Color(android.graphics.Color.parseColor(state.textStyleConfig.textColorHex))
                    }.getOrDefault(TextPureWhite)
                    val parsedBgColor = runCatching {
                        Color(android.graphics.Color.parseColor(state.textStyleConfig.backgroundColorHex))
                    }.getOrDefault(Color.Black)

                    val glassOpacity = state.textStyleConfig.backgroundOpacity.coerceIn(0f, 0.75f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(boxAlignment)
                            .padding(
                                top = if (state.textStyleConfig.textPosition == TextPosition.TOP) 72.dp else 16.dp,
                                bottom = if (state.textStyleConfig.textPosition == TextPosition.BOTTOM) 52.dp else 16.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        parsedBgColor.copy(alpha = glassOpacity),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = if (glassOpacity > 0.02f) 0.18f else 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        // CRITICAL: RTL Direction + Amiri Quran font + Ornate ﴿١﴾ brackets
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = activeAyah.formattedUthmaniWithNumber(),
                                color = parsedTextColor,
                                fontFamily = selectedArabicFontFamily,
                                fontSize = state.textStyleConfig.arabicFontSizeSp.sp,
                                lineHeight = (state.textStyleConfig.arabicFontSizeSp * 1.55f).sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    textDirection = TextDirection.Rtl,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 8f
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (state.textStyleConfig.showEnglishTranslation && activeAyah.translationText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = activeAyah.translationText,
                                color = TextPureWhite.copy(alpha = 0.92f),
                                fontFamily = selectedEnglishFontFamily,
                                fontSize = state.textStyleConfig.translationFontSizeSp.sp,
                                lineHeight = (state.textStyleConfig.translationFontSizeSp * 1.38f).sp,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.70f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Reciter & Cache Status pill inside Reel (Frosted Glass Pill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${state.selectedReciter.name} • ${activeAyah?.everyAyahFileName ?: ""}",
                    color = AuroraTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sequential Playback Transport Controls inside a GlassmorphicCard
        GlassmorphicCard(
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onStepAyah(-1) }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Ayah", tint = AuroraTeal)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuroraButtonGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable(onClick = onTogglePlay)
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("preview_play_pause_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlayingPreview) "Pause" else "Play",
                            tint = TextPureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isPlayingPreview) "Pause" else "Play Audio",
                            color = TextPureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                IconButton(onClick = { onStepAyah(1) }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next Ayah", tint = AuroraTeal)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Button: Vibrant Purple-to-Cyan glowing "Generate" button
        when (val exportState = state.exportState) {
            is ExportUiState.Idle -> {
                AuroraGradientButton(
                    text = "Generate",
                    leadingIcon = Icons.Default.PlayArrow,
                    onClick = onStartMp4Export,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "generate_reel_button"
                )
            }
            is ExportUiState.Processing -> {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exportState.statusMessage,
                                color = TextPureWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(exportState.progress * 100).roundToInt()}%",
                                color = AuroraTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { exportState.progress },
                            color = AuroraTeal,
                            trackColor = GlassCardElevatedFill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
            is ExportUiState.Completed -> {
                GlassmorphicCard(
                    borderColor = AuroraTeal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AuroraTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Saved 1080×1920 MP4 to Gallery!",
                                    color = TextPureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Movies/AyahStudio/${exportState.result.file.name}",
                                    color = TextSoftGray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        val shareUri = exportState.result.galleryUri
                        if (shareUri != null) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Ayah Studio Reel"))
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = AuroraTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", color = AuroraTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(onClick = onResetExport) {
                                Text("Done", color = AuroraTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            is ExportUiState.Error -> {
                GlassmorphicCard(
                    borderColor = AuroraMagenta,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exportState.englishMessage,
                                color = AuroraMagenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = exportState.arabicMessage,
                                color = TextSoftGray,
                                fontSize = 11.sp
                            )
                        }
                        OutlinedButton(onClick = onStartMp4Export) {
                            Text("Retry", color = AuroraTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Footer: Info icon with text "Your reel will be in 9:16 format, perfect for social media."
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextSoftGray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your reel will be in 9:16 format, perfect for social media.",
                color = TextSoftGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun QuickDesignChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GlassCardFill)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuroraTeal,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = TextPureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun buildComposeColorFilter(filter: VideoFilterConfig): ColorFilter {
    val satMatrix = ColorMatrix().apply {
        setToSaturation(filter.saturation.coerceIn(0f, 2f))
    }
    val c = filter.contrast.coerceIn(0.5f, 2.0f)
    val b = (filter.brightness.coerceIn(-100f, 100f) / 100f) * 140f
    val translate = (-.5f * c + .5f) * 255f + b
    val cbMatrix = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, translate,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )
    satMatrix.timesAssign(cbMatrix)
    return ColorFilter.colorMatrix(satMatrix)
}
