package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ArabicFontOption
import com.example.data.BackgroundScene
import com.example.data.EnglishFontOption
import com.example.data.TextPosition
import com.example.data.TextStyleConfig
import com.example.data.VideoFilterConfig
import com.example.ui.theme.AuroraButtonGradient
import com.example.ui.theme.AuroraMagenta
import com.example.ui.theme.AuroraPurple
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardElevatedFill
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.MidnightBgStart
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSoftGray
import kotlin.math.roundToInt

/**
 * Pexels Background Selection BottomSheet — Midnight Aurora & Glassmorphism Aesthetic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundSelectionModalSheet(
    pexelsVideos: List<BackgroundScene>,
    searchQuery: String,
    isSearchingPexels: Boolean,
    userVideos: List<BackgroundScene>,
    selectedBackground: BackgroundScene,
    onSearchQueryChange: (String) -> Unit,
    onSelectBackground: (BackgroundScene) -> Unit,
    onPickGalleryMedia: (android.net.Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Pexels Videos, 1 = Your Videos
    val quickTopics = listOf("All", "Night Sky", "Ocean", "Nature", "Clouds", "Rain", "Desert", "Space")

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickGalleryMedia(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBgStart,
        contentColor = TextPureWhite
    ) {
        MidnightAuroraBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Select Background",
                            color = TextPureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Search Pexels HD portrait videos or upload your own",
                            color = TextSoftGray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSoftGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Glassmorphic Tabs for "Pexels Videos" and "Your Videos"
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = GlassCardElevatedFill,
                    contentColor = AuroraTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AuroraTeal
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "Pexels Videos",
                                color = if (selectedTabIndex == 0) AuroraTeal else TextSoftGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "Your Videos",
                                color = if (selectedTabIndex == 1) AuroraTeal else TextSoftGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // Pexels API Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text("Search Pexels videos (e.g. Night Sky, Ocean, Rain)...", color = TextSoftGray, fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AuroraTeal)
                        },
                        trailingIcon = {
                            if (isSearchingPexels) {
                                CircularProgressIndicator(
                                    color = AuroraTeal,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSoftGray)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuroraTeal,
                            unfocusedBorderColor = GlassCardBorder,
                            focusedContainerColor = GlassCardFill,
                            unfocusedContainerColor = GlassCardFill,
                            focusedTextColor = TextPureWhite,
                            unfocusedTextColor = TextPureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pexels_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Pexels topic chips with Aurora gradient when active
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        quickTopics.forEach { topic ->
                            val active = (topic == "All" && searchQuery.isBlank()) ||
                                searchQuery.equals(topic, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (active) {
                                            Modifier.background(AuroraButtonGradient)
                                        } else {
                                            Modifier.background(GlassCardFill)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (active) AuroraTeal else GlassCardBorder,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        onSearchQueryChange(if (topic == "All") "" else topic)
                                    }
                                    .padding(horizontal = 13.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = topic,
                                    color = if (active) TextPureWhite else TextSoftGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // "Your Videos" tab with device gallery picker
                    AuroraGradientButton(
                        text = "Pick Video or Image from Gallery",
                        leadingIcon = Icons.Default.AddPhotoAlternate,
                        onClick = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                val sourceList = if (selectedTabIndex == 0) pexelsVideos else userVideos

                if (sourceList.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = if (selectedTabIndex == 1) {
                                "No custom videos imported yet.\nTap above to pick from your device gallery, or browse Pexels Videos."
                            } else {
                                "No Pexels videos found for \"$searchQuery\"."
                            },
                            color = TextSoftGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // 3-Column Pexels Video Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(sourceList, key = { it.videoId }) { scene ->
                            val isSelected = scene.videoId == selectedBackground.videoId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(scene.primaryHex),
                                                Color(scene.secondaryHex)
                                            )
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AuroraTeal else GlassCardBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectBackground(scene) }
                            ) {
                                if (!scene.thumbnailUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = scene.thumbnailUrl,
                                        contentDescription = scene.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.18f),
                                                        Color.Black.copy(alpha = 0.72f)
                                                    )
                                                )
                                            )
                                    )
                                }

                                // Category badge top-left
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .border(0.6.dp, GlassCardBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = scene.category,
                                        color = AuroraTeal,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Centered Play / Check Icon
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(AuroraButtonGradient)
                                            } else {
                                                Modifier.background(Color.Black.copy(alpha = 0.55f))
                                            }
                                        )
                                        .border(1.dp, AuroraTeal.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.PlayArrow,
                                        contentDescription = scene.title,
                                        tint = TextPureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Scene title & duration at bottom
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = scene.title,
                                        color = TextPureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${scene.durationLabel} • ${scene.videographer.substringAfter("• ").trim()}",
                                        color = TextSoftGray,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Text Styling BottomSheet — Midnight Aurora & Glassmorphism Aesthetic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextStylingBottomSheet(
    config: TextStyleConfig,
    onUpdateConfig: ((TextStyleConfig) -> TextStyleConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val textColorOptions = listOf("#FFFFFF", "#00F5D4", "#E2E8F0", "#FF006E", "#C084FC", "#FDE68A")
    val bgColorOptions = listOf("#0B0F19", "#1A1B2F", "#000000", "#1E1B4B", "#7B2CBF")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBgStart,
        contentColor = TextPureWhite
    ) {
        MidnightAuroraBackground(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Text Styling",
                            color = TextPureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Customize Uthmani script, glassmorphism box & translation",
                            color = TextSoftGray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSoftGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Arabic Font Toggle
                Text("Arabic Uthmani Font", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ArabicFontOption.entries.forEach { fontOpt ->
                        val active = config.arabicFont == fontOpt
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (active) {
                                        Modifier.background(AuroraButtonGradient)
                                    } else {
                                        Modifier.background(GlassCardFill)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (active) AuroraTeal else GlassCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(arabicFont = fontOpt) }
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = fontOpt.label,
                                color = if (active) TextPureWhite else TextSoftGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. English Font Toggle
                Text("English Translation Font", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EnglishFontOption.entries.forEach { engOpt ->
                        val active = config.englishFont == engOpt
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (active) {
                                        Modifier.background(AuroraButtonGradient)
                                    } else {
                                        Modifier.background(GlassCardFill)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (active) AuroraTeal else GlassCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(englishFont = engOpt) }
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = engOpt.label,
                                color = if (active) TextPureWhite else TextSoftGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Arabic Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Arabic Font Size", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "${config.arabicFontSizeSp.roundToInt()} sp",
                        color = AuroraTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = config.arabicFontSizeSp,
                    onValueChange = { newSize ->
                        val scale = ((newSize - 18f) / 3.2f).roundToInt().coerceIn(1, 10)
                        onUpdateConfig {
                            it.copy(arabicFontSizeSp = newSize, quranTextFontScale = scale)
                        }
                    },
                    valueRange = 20f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = AuroraTeal,
                        activeTrackColor = AuroraPurple,
                        inactiveTrackColor = GlassCardElevatedFill
                    )
                )

                // 4. English Translation Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Translation Font Size", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "${config.translationFontSizeSp.roundToInt()} sp",
                        color = AuroraTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = config.translationFontSizeSp,
                    onValueChange = { newSize ->
                        val scale = ((newSize - 10f) / 1.6f).roundToInt().coerceIn(1, 10)
                        onUpdateConfig {
                            it.copy(translationFontSizeSp = newSize, translationFontScale = scale)
                        }
                    },
                    valueRange = 11f..26f,
                    colors = SliderDefaults.colors(
                        thumbColor = AuroraTeal,
                        activeTrackColor = AuroraPurple,
                        inactiveTrackColor = GlassCardElevatedFill
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Text Color Picker
                Text("Verse Text Color", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    textColorOptions.forEach { hex ->
                        val selected = config.textColorHex.equals(hex, ignoreCase = true)
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) AuroraTeal else GlassCardBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(textColorHex = hex) }
                                }
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MidnightBgStart,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Glassmorphism Text Box Backdrop & Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Glassmorphism Box Opacity", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "${(config.backgroundOpacity * 100).roundToInt()}%",
                        color = AuroraTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bgColorOptions.forEach { hex ->
                        val selected = config.backgroundColorHex.equals(hex, ignoreCase = true)
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) AuroraTeal else GlassCardBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(backgroundColorHex = hex) }
                                }
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AuroraTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Slider(
                    value = config.backgroundOpacity,
                    onValueChange = { newOpacity ->
                        onUpdateConfig { it.copy(backgroundOpacity = newOpacity) }
                    },
                    valueRange = 0f..0.75f,
                    colors = SliderDefaults.colors(
                        thumbColor = AuroraTeal,
                        activeTrackColor = AuroraPurple,
                        inactiveTrackColor = GlassCardElevatedFill
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 7. Text Position Selector (Top, Center, Bottom)
                Text("Text Positioning", color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextPosition.entries.forEach { pos ->
                        val active = config.textPosition == pos
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (active) {
                                        Modifier.background(AuroraButtonGradient)
                                    } else {
                                        Modifier.background(GlassCardFill)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (active) AuroraTeal else GlassCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(textPosition = pos) }
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = pos.label,
                                color = if (active) TextPureWhite else TextSoftGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 8. Frosted Glass Card for Show Translation Switch
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
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
                                    text = "Display English subtitle under the Arabic text",
                                    color = TextSoftGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        GlowingAuroraSwitch(
                            checked = config.showEnglishTranslation,
                            onCheckedChange = { checked ->
                                onUpdateConfig { it.copy(showEnglishTranslation = checked) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 9. Frosted Glass Card for Enable TTS Switch
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Enable TTS",
                                tint = AuroraTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Enable TTS",
                                    color = TextPureWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Narrate English translation (Gemini TTS / offline fallback)",
                                    color = TextSoftGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        GlowingAuroraSwitch(
                            checked = config.enableTtsVoiceover,
                            onCheckedChange = { checked ->
                                onUpdateConfig { it.copy(enableTtsVoiceover = checked) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AuroraGradientButton(
                    text = "Save Text Style",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Video Filters BottomSheet — Midnight Aurora & Glassmorphism Aesthetic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFiltersBottomSheet(
    filterConfig: VideoFilterConfig,
    onUpdateFilter: ((VideoFilterConfig) -> VideoFilterConfig) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBgStart,
        contentColor = TextPureWhite
    ) {
        MidnightAuroraBackground(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Video Filters",
                            color = TextPureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fine-tune Brightness, Contrast, Saturation & Blur",
                            color = TextSoftGray,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = onResetFilters) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AuroraTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = AuroraTeal, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FilterSliderCard(
                    label = "Brightness",
                    valueLabel = "${if (filterConfig.brightness > 0) "+" else ""}${filterConfig.brightness.roundToInt()}%",
                    value = filterConfig.brightness,
                    valueRange = -100f..100f,
                    testTag = "slider_brightness",
                    onValueChange = { v -> onUpdateFilter { it.copy(brightness = v) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FilterSliderCard(
                    label = "Contrast",
                    valueLabel = "${(filterConfig.contrast * 100).roundToInt()}%",
                    value = filterConfig.contrast,
                    valueRange = 0.5f..2.0f,
                    testTag = "slider_contrast",
                    onValueChange = { v -> onUpdateFilter { it.copy(contrast = v) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FilterSliderCard(
                    label = "Saturation",
                    valueLabel = "${(filterConfig.saturation * 100).roundToInt()}%",
                    value = filterConfig.saturation,
                    valueRange = 0.0f..2.0f,
                    testTag = "slider_saturation",
                    onValueChange = { v -> onUpdateFilter { it.copy(saturation = v) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FilterSliderCard(
                    label = "Background Blur",
                    valueLabel = "${filterConfig.blurRadius.roundToInt()} px",
                    value = filterConfig.blurRadius,
                    valueRange = 0f..25f,
                    testTag = "slider_blur",
                    onValueChange = { v -> onUpdateFilter { it.copy(blurRadius = v) } }
                )

                Spacer(modifier = Modifier.height(18.dp))

                AuroraGradientButton(
                    text = "Apply Video Filters",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FilterSliderCard(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = TextPureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(valueLabel, color = AuroraTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = AuroraTeal,
                    activeTrackColor = AuroraPurple,
                    inactiveTrackColor = GlassCardElevatedFill
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

/**
 * Info, Quran.com/media URL Deep-Link & Flutter Code Deliverable Inspector Sheet (Midnight Aurora)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoAndFlutterDeliverableSheet(
    quranComMediaUrl: String,
    cachedMp3Count: Int,
    onApplyUrl: (String) -> Unit,
    onReopenOnboarding: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var urlInput by remember(quranComMediaUrl) { mutableStateOf(quranComMediaUrl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBgStart,
        contentColor = TextPureWhite
    ) {
        MidnightAuroraBackground(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ayah Studio & Deliverables",
                        color = TextPureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSoftGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onReopenOnboarding()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View 3-Screen Onboarding Carousel", color = AuroraTeal, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quran.com /media Deep-Link URL Sync",
                    color = TextPureWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuroraTeal,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedContainerColor = GlassCardFill,
                        unfocusedContainerColor = GlassCardFill,
                        focusedTextColor = TextPureWhite,
                        unfocusedTextColor = TextPureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraGradientButton(
                        text = "Load URL Preset",
                        onClick = {
                            onApplyUrl(urlInput)
                            Toast.makeText(context, "Applied preset!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Ayah Studio URL", quranComMediaUrl))
                            Toast.makeText(context, "Copied URL to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AuroraTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", color = AuroraTeal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Midnight Aurora Architecture & Cache",
                            color = AuroraTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Theme: Midnight Aurora (#0B0F19 -> #1A1B2F) + Purple (#7B2CBF), Teal (#00F5D4) & Magenta (#FF006E) Orbs\n" +
                                "• Glassmorphism Cards: Frosted 5% white fill + 10% white border\n" +
                                "• Quran Text: 100% Authentic Uthmani (Amiri Quran RTL ﴿١﴾)\n" +
                                "• Granular Audio Cache: $cachedMp3Count EveryAyah MP3s cached locally",
                            color = TextSoftGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
