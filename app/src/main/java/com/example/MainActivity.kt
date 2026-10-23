package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.configureAppCheck
import com.example.ui.BackgroundSelectionModalSheet
import com.example.ui.CreateReelMainScreen
import com.example.ui.InfoAndFlutterDeliverableSheet
import com.example.ui.MidnightAuroraBackground
import com.example.ui.OnboardingScreen
import com.example.ui.QuranReelsViewModel
import com.example.ui.TextStylingBottomSheet
import com.example.ui.VideoFiltersBottomSheet
import com.example.ui.theme.MidnightBgStart
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // STEP 2: Enable Edge-to-Edge with dark status/navigation bar style for Midnight Aurora
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightBgStart
                ) {
                    AyahStudioRootApp()
                }
            }
        }
    }
}

@Composable
fun AyahStudioRootApp(
    viewModel: QuranReelsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Full-bleed Midnight Aurora Background (#0B0F19 -> #1A1B2F + blurred Purple/Teal/Magenta orbs)
    MidnightAuroraBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                if (state.showOnboarding) {
                    OnboardingScreen(
                        currentPage = state.onboardingPage,
                        onPageChange = viewModel::setOnboardingPage,
                        onSkipOrFinish = viewModel::skipOrFinishOnboarding
                    )
                } else {
                    CreateReelMainScreen(
                        state = state,
                        onSelectWizardStep = viewModel::setWizardStep,
                        onNextStep = viewModel::nextWizardStep,
                        onPrevStep = viewModel::previousWizardStep,
                        onToggleLanguage = viewModel::toggleLanguageCode,
                        onOpenInfoSheet = { viewModel.openFlutterCodeSheet(true) },
                        onSelectReciter = viewModel::selectReciter,
                        onSelectSurah = viewModel::selectSurah,
                        onIncrementStartAyah = viewModel::incrementStartAyah,
                        onDecrementStartAyah = viewModel::decrementStartAyah,
                        onIncrementNumberOfAyahs = viewModel::incrementNumberOfAyahs,
                        onDecrementNumberOfAyahs = viewModel::decrementNumberOfAyahs,
                        onSelectTranslation = viewModel::selectTranslation,
                        onToggleTranslationVisibility = viewModel::toggleTranslationVisibility,
                        onToggleMushafPage = viewModel::toggleMushafPage,
                        onOpenBackgroundModal = { viewModel.openBackgroundModal(true) },
                        onOpenTextStyleSheet = { viewModel.openTextStyleSheet(true) },
                        onOpenVideoFiltersSheet = { viewModel.openVideoFiltersSheet(true) },
                        onTogglePlayPreview = viewModel::togglePreviewPlayback,
                        onStepPreviewAyah = viewModel::stepPreviewAyah,
                        onStartMp4Export = viewModel::startMp4Export,
                        onResetExport = viewModel::resetExportState
                    )
                }
            }
        }
    }

    // Pexels Background Selection BottomSheet (Midnight Aurora Glassmorphism)
    if (state.showBackgroundModal) {
        BackgroundSelectionModalSheet(
            pexelsVideos = state.pexelsVideos,
            searchQuery = state.pexelsSearchQuery,
            isSearchingPexels = state.isSearchingPexels,
            userVideos = state.userGalleryVideos,
            selectedBackground = state.selectedBackground,
            onSearchQueryChange = viewModel::searchPexelsBackgrounds,
            onSelectBackground = viewModel::selectBackgroundScene,
            onPickGalleryMedia = viewModel::addCustomGalleryBackground,
            onDismiss = { viewModel.openBackgroundModal(false) }
        )
    }

    // Text Styling BottomSheet (Midnight Aurora Glassmorphism)
    if (state.showTextStyleSheet) {
        TextStylingBottomSheet(
            config = state.textStyleConfig,
            onUpdateConfig = viewModel::updateTextStyle,
            onDismiss = { viewModel.openTextStyleSheet(false) }
        )
    }

    // Video Filters BottomSheet (Midnight Aurora Glassmorphism)
    if (state.showVideoFiltersSheet) {
        VideoFiltersBottomSheet(
            filterConfig = state.videoFilterConfig,
            onUpdateFilter = viewModel::updateVideoFilterConfig,
            onResetFilters = viewModel::resetVideoFilters,
            onDismiss = { viewModel.openVideoFiltersSheet(false) }
        )
    }

    // Header Info Modal: Re-open Onboarding, Quran.com/media URL Deep-Link Sync & Deliverables
    if (state.showFlutterCodeSheet) {
        InfoAndFlutterDeliverableSheet(
            quranComMediaUrl = state.quranComMediaUrl,
            cachedMp3Count = state.totalCachedMp3FilesOnDisk,
            onApplyUrl = viewModel::applyQuranComMediaUrl,
            onReopenOnboarding = viewModel::reopenOnboarding,
            onDismiss = { viewModel.openFlutterCodeSheet(false) }
        )
    }
}
