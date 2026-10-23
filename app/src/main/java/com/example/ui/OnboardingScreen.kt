package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuroraButtonGradient
import com.example.ui.theme.AuroraMagenta
import com.example.ui.theme.AuroraPurple
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.MidnightBgStart
import com.example.ui.theme.MushafRecitedRed
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSoftGray

private data class OnboardingPageSpec(
    val title: String,
    val subtitle: String,
    val pageIndex: Int
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPageSpec(
        title = "Next-Gen Quran Reels Studio",
        subtitle = "Craft vertical 9:16 Quran reels with 100% authentic Uthmani script, Pexels HD backgrounds, and glassmorphism.",
        pageIndex = 0
    ),
    OnboardingPageSpec(
        title = "World-Renowned Reciters & Mushaf Mode",
        subtitle = "Select your favorite Qari, cache only the Ayahs you need, and highlight the recited Ayah in real time.",
        pageIndex = 1
    ),
    OnboardingPageSpec(
        title = "Customize & Export 1080p MP4s",
        subtitle = "Style Arabic Uthmani fonts, frosted glass text boxes, and video filters, then export ready for social media.",
        pageIndex = 2
    )
)

@Composable
fun OnboardingScreen(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onSkipOrFinish: () -> Unit
) {
    val pageSpec = ONBOARDING_PAGES[currentPage.coerceIn(0, 2)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top-right "Skip" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onSkipOrFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    color = TextSoftGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Centered custom Glassmorphic illustration
        AnimatedContent(
            targetState = pageSpec,
            label = "onboarding_slide"
        ) { spec ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                OnboardingIllustration(pageIndex = spec.pageIndex)

                Spacer(modifier = Modifier.height(36.dp))

                // Title (Pure White #FFFFFF, bold)
                Text(
                    text = spec.title,
                    color = TextPureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subtitle (Soft Gray #A0AAB2)
                Text(
                    text = spec.subtitle,
                    color = TextSoftGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Glowing Pagination dots at the bottom
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 28.dp)
        ) {
            for (i in 0..2) {
                val isSelected = i == currentPage
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 28.dp else 10.dp,
                    animationSpec = tween(220),
                    label = "dot_width"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(dotWidth)
                        .height(10.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(AuroraButtonGradient)
                            } else {
                                Modifier.background(GlassCardBorder)
                            }
                        )
                        .clickable { onPageChange(i) }
                )
            }
        }

        // Vibrant Purple-to-Cyan glowing "Next" / "Get Started" button
        AuroraGradientButton(
            text = if (currentPage < 2) "Next" else "Get Started",
            onClick = {
                if (currentPage < 2) {
                    onPageChange(currentPage + 1)
                } else {
                    onSkipOrFinish()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "onboarding_next_button"
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OnboardingIllustration(pageIndex: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(GlassCardFill)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(AuroraPurple, AuroraTeal, AuroraMagenta)
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glowing Aurora Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuroraPurple.copy(alpha = 0.28f),
                        AuroraTeal.copy(alpha = 0.14f),
                        Color.Transparent
                    )
                ),
                radius = w * 0.44f,
                center = Offset(w / 2f, h / 2f)
            )
            drawCircle(
                color = AuroraTeal.copy(alpha = 0.45f),
                radius = w * 0.38f,
                center = Offset(w / 2f, h / 2f),
                style = Stroke(width = 2.dp.toPx())
            )

            when (pageIndex) {
                0 -> {
                    // 9:16 vertical phone frame with Neon Teal & Purple verse lines
                    val phoneW = w * 0.38f
                    val phoneH = h * 0.64f
                    val left = (w - phoneW) / 2f
                    val top = (h - phoneH) / 2f
                    drawRoundRect(
                        color = MidnightBgStart,
                        topLeft = Offset(left, top),
                        size = Size(phoneW, phoneH),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    drawRoundRect(
                        color = AuroraTeal,
                        topLeft = Offset(left, top),
                        size = Size(phoneW, phoneH),
                        cornerRadius = CornerRadius(18f, 18f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = AuroraTeal,
                        topLeft = Offset(left + phoneW * 0.18f, top + phoneH * 0.38f),
                        size = Size(phoneW * 0.64f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(left + phoneW * 0.24f, top + phoneH * 0.49f),
                        size = Size(phoneW * 0.52f, 5.dp.toPx()),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                1 -> {
                    // Open Mushaf Page with highlighted Ayah
                    val bookW = w * 0.56f
                    val bookH = h * 0.46f
                    val left = (w - bookW) / 2f
                    val top = (h - bookH) / 2f
                    drawRoundRect(
                        color = Color(0xFFFDFBF7),
                        topLeft = Offset(left, top),
                        size = Size(bookW, bookH),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = AuroraTeal,
                        topLeft = Offset(left, top),
                        size = Size(bookW, bookH),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = MidnightBgStart,
                        topLeft = Offset(left + 20f, top + bookH * 0.26f),
                        size = Size(bookW - 40f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = MushafRecitedRed,
                        topLeft = Offset(left + 20f, top + bookH * 0.48f),
                        size = Size(bookW - 40f, 8.dp.toPx()),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = MidnightBgStart,
                        topLeft = Offset(left + 20f, top + bookH * 0.70f),
                        size = Size(bookW - 40f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                else -> {
                    // Export Reel badge with Play triangle
                    val playPath = Path().apply {
                        moveTo(w * 0.43f, h * 0.37f)
                        lineTo(w * 0.62f, h * 0.50f)
                        lineTo(w * 0.43f, h * 0.63f)
                        close()
                    }
                    drawPath(playPath, color = AuroraTeal)
                }
            }
        }

        // Badge icon in corner
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(AuroraButtonGradient)
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = when (pageIndex) {
                    0 -> Icons.Default.AutoAwesome
                    1 -> Icons.Default.MenuBook
                    else -> Icons.Default.MovieCreation
                },
                contentDescription = null,
                tint = TextPureWhite,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
