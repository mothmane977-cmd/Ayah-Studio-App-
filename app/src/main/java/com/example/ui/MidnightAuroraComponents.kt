package com.example.ui

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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuroraButtonGradient
import com.example.ui.theme.AuroraMagenta
import com.example.ui.theme.AuroraPurple
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.MidnightBackgroundGradient
import com.example.ui.theme.TextPureWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. The "Midnight Aurora" Background:
 * - Rich, deep dark blue/black gradient (#0B0F19 to #1A1B2F)
 * - Subtle, blurred, glowing Aurora orbs in the background
 *   (Purple #7B2CBF, Teal #00F5D4, and Magenta #FF006E)
 */
@Composable
fun MidnightAuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_orbs")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBackgroundGradient)
    ) {
        // Blurred glowing Aurora Orbs (Purple #7B2CBF, Teal #00F5D4, Magenta #FF006E)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-Left Purple Orb (#7B2CBF)
            val purpleCenter = Offset(
                x = w * 0.18f + cos(phase) * 28f,
                y = h * 0.14f + sin(phase) * 22f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuroraPurple.copy(alpha = 0.34f),
                        AuroraPurple.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = purpleCenter,
                    radius = w * 0.68f
                ),
                radius = w * 0.68f,
                center = purpleCenter
            )

            // Center-Right Neon Teal Orb (#00F5D4)
            val tealCenter = Offset(
                x = w * 0.85f - sin(phase * 0.8f) * 32f,
                y = h * 0.46f + cos(phase * 0.8f) * 26f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuroraTeal.copy(alpha = 0.24f),
                        AuroraTeal.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = tealCenter,
                    radius = w * 0.64f
                ),
                radius = w * 0.64f,
                center = tealCenter
            )

            // Bottom-Left Magenta Orb (#FF006E)
            val magentaCenter = Offset(
                x = w * 0.25f + sin(phase * 1.1f) * 24f,
                y = h * 0.84f - cos(phase * 1.1f) * 24f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuroraMagenta.copy(alpha = 0.22f),
                        AuroraPurple.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = magentaCenter,
                    radius = w * 0.66f
                ),
                radius = w * 0.66f,
                center = magentaCenter
            )
        }

        content()
    }
}

/**
 * 2. Glassmorphism Card:
 * - Transparent frosted glass surface: Colors.white.withOpacity(0.05)
 * - 1px solid border: Colors.white.withOpacity(0.10)
 * - Subtle white/aurora outer glow
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassCardBorder,
    glowColor: Color = Color.White.copy(alpha = 0.06f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .drawBehind {
                // Subtle outer white/aurora glow
                drawRoundRect(
                    color = glowColor,
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width + 4.dp.toPx(),
                        height = size.height + 4.dp.toPx()
                    ),
                    cornerRadius = CornerRadius((cornerRadius + 2.dp).toPx())
                )
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.07f),
                        GlassCardFill,
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

/**
 * 3. Vibrant, Glowing Gradient Button:
 * - LinearGradient(colors: [Color(0xFF7B2CBF), Color(0xFF00F5D4)])
 * - Glowing boxShadow with Purple & Cyan aura
 * - Crisp Pure White (#FFFFFF) text & icon
 */
@Composable
fun AuroraGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(54.dp)
            .drawBehind {
                // Vibrant Purple-to-Cyan glowing boxShadow
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AuroraPurple.copy(alpha = 0.45f),
                            AuroraTeal.copy(alpha = 0.45f)
                        )
                    ),
                    topLeft = Offset(0f, 3.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(shape)
            .background(AuroraButtonGradient)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        AuroraTeal.copy(alpha = 0.7f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextPureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = TextPureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = TextPureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
