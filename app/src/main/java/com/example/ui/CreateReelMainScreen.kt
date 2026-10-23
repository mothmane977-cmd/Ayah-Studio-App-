package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ReciterOption
import com.example.data.SurahInfo
import com.example.data.TranslationLanguageOption
import com.example.data.WizardStep
import com.example.ui.theme.AuroraButtonGradient
import com.example.ui.theme.AuroraMagenta
import com.example.ui.theme.AuroraPurple
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardElevatedFill
import com.example.ui.theme.MidnightDeepSurface
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSoftGray
import kotlin.math.roundToInt

/**
 * "Ayah Studio" 3-Step Wizard Main Screen — Midnight Aurora & Glassmorphism Aesthetic:
 * - Background: Deep Midnight Aurora (#0B0F19 to #1A1B2F) with blurred Purple (#7B2CBF),
 *   Teal (#00F5D4), and Magenta (#FF006E) glowing orbs.
 * - Cards: Transparent frosted glass (Colors.white.withOpacity(0.05), 1px border Colors.white.withOpacity(0.10))
 *   with subtle outer white/aurora glow.
 * - Main Buttons & Active States: Vibrant Purple-to-Cyan gradient (#7B2CBF -> #00F5D4) with glowing aura.
 * - Typography: Pure White (#FFFFFF) headings & Soft Gray (#A0AAB2) subtitles.
 */
@Composable
fun CreateReelMainScreen(
    state: QuranReelsUiState,
    onSelectWizardStep: (WizardStep) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenInfoSheet: () -> Unit,
    onSelectReciter: (ReciterOption) -> Unit,
    onSelectSurah: (SurahInfo) -> Unit,
    onIncrementStartAyah: () -> Unit,
    onDecrementStartAyah: () -> Unit,
    onIncrementNumberOfAyahs: () -> Unit,
    onDecrementNumberOfAyahs: () -> Unit,
    onSelectTranslation: (TranslationLanguageOption) -> Unit,
    onToggleTranslationVisibility: (Boolean) -> Unit,
    onToggleMushafPage: (Boolean) -> Unit,
    onOpenBackgroundModal: () -> Unit,
    onOpenTextStyleSheet: () -> Unit,
    onOpenVideoFiltersSheet: () -> Unit,
    onTogglePlayPreview: () -> Unit,
    onStepPreviewAyah: (Int) -> Unit,
    onStartMp4Export: () -> Unit,
    onResetExport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header: Glowing "ق" emblem, "Ayah Studio" in Pure White, "EN" glass pill and Info icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AuroraTeal.copy(alpha = 0.5f),
                                    AuroraPurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width * 0.78f
                        )
                    }
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFF0A1128))
                    .border(1.dp, AuroraTeal.copy(alpha = 0.65f), RoundedCornerShape(11.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Ayah Studio Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ayah Studio",
                    color = TextPureWhite,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step ${state.currentWizardStep.stepNumber} of 3 • ${state.currentWizardStep.subtitle}",
                    color = TextSoftGray,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // Frosted Glass "EN" button with Neon Teal globe icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassCardElevatedFill)
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("language_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = AuroraTeal,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = state.uiLanguageCode,
                    color = TextPureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onOpenInfoSheet,
                modifier = Modifier.testTag("header_info_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info & Deliverables",
                    tint = AuroraTeal
                )
            }
        }

        // 3-Step Glassmorphic Wizard Progress Bar: 1. Content -> 2. Design -> 3. Preview
        WizardStepProgressBar(
            currentStep = state.currentWizardStep,
            onSelectStep = onSelectWizardStep
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step Body
        Box(modifier = Modifier.weight(1f)) {
            when (state.currentWizardStep) {
                WizardStep.CONTENT -> {
                    Step1ContentScreen(
                        state = state,
                        onSelectReciter = onSelectReciter,
                        onSelectSurah = onSelectSurah,
                        onIncrementStartAyah = onIncrementStartAyah,
                        onDecrementStartAyah = onDecrementStartAyah,
                        onIncrementNumberOfAyahs = onIncrementNumberOfAyahs,
                        onDecrementNumberOfAyahs = onDecrementNumberOfAyahs,
                        onSelectTranslation = onSelectTranslation,
                        onToggleTranslationVisibility = onToggleTranslationVisibility,
                        onToggleMushafPage = onToggleMushafPage,
                        onNextStep = onNextStep
                    )
                }
                WizardStep.DESIGN -> {
                    Step2DesignScreen(
                        state = state,
                        onOpenBackgroundModal = onOpenBackgroundModal,
                        onOpenTextStyleSheet = onOpenTextStyleSheet,
                        onOpenVideoFiltersSheet = onOpenVideoFiltersSheet,
                        onPrevStep = onPrevStep,
                        onNextStep = onNextStep
                    )
                }
                WizardStep.PREVIEW -> {
                    LivePreviewAndExportScreen(
                        state = state,
                        onBack = onPrevStep,
                        onTogglePlay = onTogglePlayPreview,
                        onStepAyah = onStepPreviewAyah,
                        onOpenBackgrounds = onOpenBackgroundModal,
                        onOpenTextStyle = onOpenTextStyleSheet,
                        onOpenFilters = onOpenVideoFiltersSheet,
                        onStartMp4Export = onStartMp4Export,
                        onResetExport = onResetExport
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardStepProgressBar(
    currentStep: WizardStep,
    onSelectStep: (WizardStep) -> Unit
) {
    GlassmorphicCard(
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WizardStep.entries.forEach { step ->
                val isActive = step == currentStep
                val isCompleted = step.stepNumber < currentStep.stepNumber
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isActive) {
                                Modifier.drawBehind {
                                    drawRoundRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                AuroraPurple.copy(alpha = 0.42f),
                                                AuroraTeal.copy(alpha = 0.42f)
                                            )
                                        ),
                                        cornerRadius = CornerRadius(10.dp.toPx())
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isActive) {
                                Modifier.background(AuroraButtonGradient)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .border(
                            width = if (isActive || isCompleted) 1.dp else 0.dp,
                            color = when {
                                isActive -> Color.White.copy(alpha = 0.35f)
                                isCompleted -> AuroraTeal.copy(alpha = 0.45f)
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectStep(step) }
                        .padding(vertical = 9.dp, horizontal = 6.dp)
                        .testTag("wizard_step_${step.stepNumber}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> Color.Black.copy(alpha = 0.28f)
                                    isCompleted -> AuroraTeal.copy(alpha = 0.25f)
                                    else -> GlassCardElevatedFill
                                }
                            )
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AuroraTeal,
                                modifier = Modifier.size(13.dp)
                            )
                        } else {
                            Text(
                                text = "${step.stepNumber}",
                                color = if (isActive) TextPureWhite else TextSoftGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = step.title,
                        color = if (isActive) TextPureWhite else TextSoftGray,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * STEP 1 (Content):
 * - Frosted Glassmorphic Cards for Reciter, Surah, Ayah Range, Show Translation & Mushaf Mode
 * - Vibrant Purple-to-Cyan glowing CTA button
 */
@Composable
private fun Step1ContentScreen(
    state: QuranReelsUiState,
    onSelectReciter: (ReciterOption) -> Unit,
    onSelectSurah: (SurahInfo) -> Unit,
    onIncrementStartAyah: () -> Unit,
    onDecrementStartAyah: () -> Unit,
    onIncrementNumberOfAyahs: () -> Unit,
    onDecrementNumberOfAyahs: () -> Unit,
    onSelectTranslation: (TranslationLanguageOption) -> Unit,
    onToggleTranslationVisibility: (Boolean) -> Unit,
    onToggleMushafPage: (Boolean) -> Unit,
    onNextStep: () -> Unit
) {
    var reciterDropdownExpanded by remember { mutableStateOf(false) }
    var surahDropdownExpanded by remember { mutableStateOf(false) }
    var translationDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("step_1_content_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Card 1: Select Reciter (Glassmorphism)
        item {
            Box {
                DropdownCardItem(
                    icon = Icons.Default.Mic,
                    label = "Select Reciter",
                    selectedValue = state.selectedReciter.name,
                    testTag = "card_select_reciter",
                    onClick = { reciterDropdownExpanded = true }
                )
                DropdownMenu(
                    expanded = reciterDropdownExpanded,
                    onDismissRequest = { reciterDropdownExpanded = false },
                    modifier = Modifier
                        .background(MidnightDeepSurface)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(8.dp))
                ) {
                    state.reciters.forEach { reciter ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(reciter.name, color = TextPureWhite, fontWeight = FontWeight.SemiBold)
                                    Text("${reciter.arabicName} • ${reciter.style}", color = TextSoftGray, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                reciterDropdownExpanded = false
                                onSelectReciter(reciter)
                            }
                        )
                    }
                }
            }
        }

        // Card 2: Select Surah (Glassmorphism)
        item {
            Box {
                DropdownCardItem(
                    icon = Icons.Default.Tag,
                    label = "Select Surah",
                    selectedValue = state.selectedSurah.displayLabel,
                    testTag = "card_select_surah",
                    onClick = { surahDropdownExpanded = true }
                )
                DropdownMenu(
                    expanded = surahDropdownExpanded,
                    onDismissRequest = { surahDropdownExpanded = false },
                    modifier = Modifier
                        .background(MidnightDeepSurface)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(8.dp))
                        .height(340.dp)
                ) {
                    state.chapters.forEach { surah ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = surah.displayLabel,
                                        color = TextPureWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${surah.versesCount} Ayah",
                                        color = AuroraTeal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                surahDropdownExpanded = false
                                onSelectSurah(surah)
                            }
                        )
                    }
                }
            }
        }

        // Card 3: Ayah Range with glowing Purple/Cyan Ayah count badge + Steppers
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_ayah_range")
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ayah Range",
                                color = TextPureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Selected: Ayah ${state.startAyah} to ${state.endAyah}",
                                color = TextSoftGray,
                                fontSize = 12.sp
                            )
                        }
                        // Glowing Aurora Ayah Count Indicator pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AuroraPurple.copy(alpha = 0.35f),
                                            AuroraTeal.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                                .border(1.dp, AuroraTeal.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${state.selectedSurah.versesCount} Ayah",
                                color = AuroraTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AyahStepperRow(
                        label = "Start Ayah",
                        value = state.startAyah,
                        minusTag = "start_ayah_minus",
                        plusTag = "start_ayah_plus",
                        onDecrement = onDecrementStartAyah,
                        onIncrement = onIncrementStartAyah
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AyahStepperRow(
                        label = "Number of Ayahs",
                        value = state.numberOfAyahs,
                        minusTag = "num_ayahs_minus",
                        plusTag = "num_ayahs_plus",
                        onDecrement = onDecrementNumberOfAyahs,
                        onIncrement = onIncrementNumberOfAyahs
                    )
                }
            }
        }

        // Card 4: Show Translation Toggle & Language Dropdown (Glassmorphism + Glowing Switch)
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Show Translation",
                                tint = AuroraTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Show Translation",
                                    color = TextPureWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Display English translation under the Arabic verse",
                                    color = TextSoftGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        GlowingAuroraSwitch(
                            checked = state.textStyleConfig.showEnglishTranslation,
                            onCheckedChange = onToggleTranslationVisibility,
                            testTag = "translation_toggle_switch"
                        )
                    }

                    if (state.textStyleConfig.showEnglishTranslation) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GlassCardElevatedFill)
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { translationDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${state.selectedTranslation.languageName} — ${state.selectedTranslation.translatorName}",
                                    color = TextPureWhite,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select translation",
                                    tint = AuroraTeal
                                )
                            }
                            DropdownMenu(
                                expanded = translationDropdownExpanded,
                                onDismissRequest = { translationDropdownExpanded = false },
                                modifier = Modifier
                                    .background(MidnightDeepSurface)
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(8.dp))
                            ) {
                                state.translationOptions.filter { it.id != 0 }.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(option.languageName, color = TextPureWhite, fontWeight = FontWeight.SemiBold)
                                                Text(option.translatorName, color = TextSoftGray, fontSize = 12.sp)
                                            }
                                        },
                                        onClick = {
                                            translationDropdownExpanded = false
                                            onSelectTranslation(option)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 5: Mushaf Page Mode Toggle (Glassmorphism + Glowing Switch)
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_mushaf_page")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Mushaf Page Mode",
                        tint = AuroraTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mushaf Page Mode",
                            color = TextPureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Show the Quran page with recited Ayah highlighted",
                            color = TextSoftGray,
                            fontSize = 12.sp
                        )
                    }
                    GlowingAuroraSwitch(
                        checked = state.showMushafPage,
                        onCheckedChange = onToggleMushafPage,
                        testTag = "mushaf_page_switch"
                    )
                }
            }
        }

        // Step 1 -> Step 2 Vibrant Aurora Gradient Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            AuroraGradientButton(
                text = "Next: Customize Design",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onNextStep,
                modifier = Modifier.fillMaxWidth(),
                testTag = "next_to_design_button"
            )
        }
    }
}

/**
 * STEP 2 (Design):
 * Frosted Glassmorphic Cards for:
 * 1. "Select Background" (Pexels API search & 3-column video grid)
 * 2. "Text Style" (Arabic/English font toggles, size sliders, color pickers, positioning)
 * 3. "Video Filters" (Brightness, Contrast, Saturation, Blur sliders)
 */
@Composable
private fun Step2DesignScreen(
    state: QuranReelsUiState,
    onOpenBackgroundModal: () -> Unit,
    onOpenTextStyleSheet: () -> Unit,
    onOpenVideoFiltersSheet: () -> Unit,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("step_2_design_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Select Background (Pexels API + Gallery) with thumbnail preview
        item {
            GlassmorphicCard(
                onClick = onOpenBackgroundModal,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_select_background")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live 9:16 Thumbnail Preview of selected Pexels background
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(86.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(state.selectedBackground.primaryHex),
                                        Color(state.selectedBackground.secondaryHex)
                                    )
                                )
                            )
                            .border(1.5.dp, AuroraTeal, RoundedCornerShape(10.dp))
                    ) {
                        if (!state.selectedBackground.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = state.selectedBackground.thumbnailUrl,
                                contentDescription = state.selectedBackground.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = AuroraTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Select Background",
                                color = TextPureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.selectedBackground.title,
                            color = AuroraTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${state.selectedBackground.videographer} • Tap to search Pexels",
                            color = TextSoftGray,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AuroraTeal
                    )
                }
            }
        }

        // 2. Text Style Card (opens Text Styling BottomSheet)
        item {
            DesignFeatureCard(
                icon = Icons.Default.FontDownload,
                title = "Text Style",
                primarySummary = "${state.textStyleConfig.arabicFont.label} + ${state.textStyleConfig.englishFont.label}",
                secondarySummary = "Arabic ${state.textStyleConfig.arabicFontSizeSp.roundToInt()}sp • Glass Opacity ${(state.textStyleConfig.backgroundOpacity * 100).roundToInt()}% • Position: ${state.textStyleConfig.textPosition.label}",
                testTag = "card_text_style",
                onClick = onOpenTextStyleSheet
            )
        }

        // 3. Video Filters Card (opens Video Filters BottomSheet with Brightness, Contrast, Saturation, Blur sliders)
        item {
            DesignFeatureCard(
                icon = Icons.Default.AutoFixHigh,
                title = "Video Filters",
                primarySummary = state.videoFilterConfig.summaryLabel,
                secondarySummary = "Adjust Brightness, Contrast, Saturation & Background Blur",
                testTag = "card_video_filters",
                onClick = onOpenVideoFiltersSheet
            )
        }

        // Navigation Row: Back to Step 1 | Next to Step 3 (Preview)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.38f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassCardElevatedFill)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onPrevStep)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = AuroraTeal)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Content", color = TextPureWhite, fontWeight = FontWeight.SemiBold)
                    }
                }

                AuroraGradientButton(
                    text = "Next: 9:16 Preview",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNextStep,
                    modifier = Modifier.weight(0.62f),
                    testTag = "next_to_preview_button"
                )
            }
        }
    }
}

@Composable
private fun DesignFeatureCard(
    icon: ImageVector,
    title: String,
    primarySummary: String,
    secondarySummary: String,
    testTag: String,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AuroraPurple.copy(alpha = 0.35f),
                                AuroraTeal.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(1.dp, AuroraTeal.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AuroraTeal,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = primarySummary,
                    color = AuroraTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = secondarySummary,
                    color = TextSoftGray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AuroraTeal
            )
        }
    }
}

@Composable
private fun DropdownCardItem(
    icon: ImageVector,
    label: String,
    selectedValue: String,
    testTag: String,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AuroraTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = TextPureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassCardElevatedFill)
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedValue,
                    color = TextPureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AuroraTeal
                )
            }
        }
    }
}

@Composable
private fun AyahStepperRow(
    label: String,
    value: Int,
    minusTag: String,
    plusTag: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextSoftGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(GlassCardElevatedFill)
                .border(1.dp, GlassCardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(36.dp)
                    .testTag(minusTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = AuroraTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value.toString(),
                color = TextPureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(36.dp)
                    .testTag(plusTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = AuroraTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GlowingAuroraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(
                if (checked) {
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AuroraTeal.copy(alpha = 0.45f),
                                    AuroraPurple.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width * 0.7f
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPureWhite,
                checkedTrackColor = AuroraTeal,
                checkedBorderColor = AuroraPurple,
                uncheckedThumbColor = TextSoftGray,
                uncheckedTrackColor = GlassCardElevatedFill,
                uncheckedBorderColor = GlassCardBorder
            ),
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}
