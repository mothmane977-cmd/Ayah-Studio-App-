package com.example.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthenticAyah
import com.example.data.BackgroundScene
import com.example.data.FRIENDLY_AUDIO_ERROR_MESSAGE_AR
import com.example.data.FRIENDLY_AUDIO_ERROR_MESSAGE_EN
import com.example.data.PexelsRepository
import com.example.data.QuranRepository
import com.example.data.ReciterOption
import com.example.data.SurahInfo
import com.example.data.TextStyleConfig
import com.example.data.TranslationLanguageOption
import com.example.data.VideoFilterConfig
import com.example.data.WizardStep
import com.example.service.ExportResult
import com.example.service.ReelVideoExportEngine
import com.example.service.ResilientTtsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ExportUiState {
    data object Idle : ExportUiState
    data class Processing(val progress: Float, val statusMessage: String) : ExportUiState
    data class Completed(val result: ExportResult) : ExportUiState
    data class Error(
        val arabicMessage: String = FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
        val englishMessage: String = FRIENDLY_AUDIO_ERROR_MESSAGE_EN
    ) : ExportUiState
}

data class QuranReelsUiState(
    // Onboarding state (3 screens)
    val showOnboarding: Boolean = false,
    val onboardingPage: Int = 0,

    // 3-Step Wizard Flow: Step 1 (Content) -> Step 2 (Design) -> Step 3 (Preview)
    val currentWizardStep: WizardStep = WizardStep.CONTENT,

    // Responsive 480px Web Frame language indicator
    val uiLanguageCode: String = "EN",

    // Step 1: Content (Reciter, Surah, Ayah Range, Translation Toggle)
    val reciters: List<ReciterOption> = ReciterOption.ALL,
    val selectedReciter: ReciterOption = ReciterOption.ALL.first(), // "Mishary Rashid Alafasy"
    val chapters: List<SurahInfo> = QuranRepository.CANONICAL_CHAPTERS_METADATA,
    val selectedSurah: SurahInfo = QuranRepository.CANONICAL_CHAPTERS_METADATA.first(),
    val startAyah: Int = 1,
    val numberOfAyahs: Int = 3,
    val translationOptions: List<TranslationLanguageOption> = TranslationLanguageOption.ALL,
    val selectedTranslation: TranslationLanguageOption = TranslationLanguageOption.ALL.first(),
    val showMushafPage: Boolean = false,

    // Step 2: Design (Pexels Backgrounds, Text Style, Video Filters)
    val hasExplicitlyChosenBackground: Boolean = false,
    val pexelsVideos: List<BackgroundScene> = BackgroundScene.PEXELS_CURATED_PACK,
    val pexelsSearchQuery: String = "",
    val isSearchingPexels: Boolean = false,
    val userGalleryVideos: List<BackgroundScene> = emptyList(),
    val selectedBackground: BackgroundScene = BackgroundScene.PEXELS_CURATED_PACK.first(),
    val textStyleConfig: TextStyleConfig = TextStyleConfig(),
    val videoFilterConfig: VideoFilterConfig = VideoFilterConfig(),

    // Authentic Quran.com API v4 verses & granular EveryAyah / Quran.com CDN MP3 cache state
    val authenticAyahs: List<AuthenticAyah> = emptyList(),
    val isLoadingVerses: Boolean = false,
    val verseLoadError: String? = null,
    val isDownloadingAudio: Boolean = false,
    val audioDownloadProgressPercent: Int = 0,
    val audioDownloadProgressLabel: String = "",
    val audioDownloadError: String? = null,
    val audioCacheStatusMessage: String = "Ready",
    val totalCachedMp3FilesOnDisk: Int = 0,

    // Step 3: Live 9:16 Preview & Sequential Playback state
    val isPlayingPreview: Boolean = false,
    val activePreviewAyahIndex: Int = 0,
    val activeTtsEngineBadge: String = "Gemini 3.8 Flash TTS / Native TTS",

    // Active BottomSheets / Modals
    val showBackgroundModal: Boolean = false,
    val showTextStyleSheet: Boolean = false,
    val showVideoFiltersSheet: Boolean = false,
    val showFlutterCodeSheet: Boolean = false,

    // Export MP4 state
    val exportState: ExportUiState = ExportUiState.Idle
) {
    val endAyah: Int
        get() = (startAyah + numberOfAyahs - 1).coerceAtMost(selectedSurah.versesCount)

    val backgroundCardSummary: String
        get() = "${selectedBackground.title} (${selectedBackground.category})"

    val textStyleCardSummary: String
        get() = "${textStyleConfig.fontFamilyStyle} • ${textStyleConfig.textPosition.label}"

    val quranComMediaUrl: String
        get() {
            val bgHexEncoded = textStyleConfig.backgroundColorHex.replace("#", "%23")
            val borderHexEncoded = textStyleConfig.borderColorHex.replace("#", "%23")
            val fontHexEncoded = textStyleConfig.textColorHex.replace("#", "%23")
            return "https://quran.com/media?surah=${selectedSurah.id}&verseFrom=$startAyah&verseTo=$endAyah" +
                "&mediaReciter=${selectedReciter.id}&mediaTranslations=${selectedTranslation.id}" +
                "&backgroundColor=$bgHexEncoded&opacity=${textStyleConfig.backgroundOpacity}" +
                "&borderColor=$borderHexEncoded&borderSize=${textStyleConfig.borderSize}" +
                "&fontColor=$fontHexEncoded&videoId=${selectedBackground.videoId}" +
                "&quranTextFontScale=${textStyleConfig.quranTextFontScale}" +
                "&quranTextFontStyle=${textStyleConfig.quranTextFontStyleKey}" +
                "&translationFontScale=${textStyleConfig.translationFontScale}" +
                "&orientation=portrait&previewMode=disabled"
        }
}

class QuranReelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application.applicationContext)
    private val pexelsRepository = PexelsRepository()
    private val ttsService = ResilientTtsService(application.applicationContext)

    private val _uiState = MutableStateFlow(QuranReelsUiState())
    val uiState: StateFlow<QuranReelsUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null
    private var exportJob: Job? = null
    private var pexelsSearchJob: Job? = null
    private var audioDownloadJob: Job? = null

    init {
        loadChaptersAndInitialVerses()
    }

    private fun loadChaptersAndInitialVerses() {
        viewModelScope.launch {
            val chapters = repository.fetchChapters()
            val currentSurah = chapters.find { it.id == _uiState.value.selectedSurah.id } ?: chapters.first()
            _uiState.update {
                it.copy(
                    chapters = chapters,
                    selectedSurah = currentSurah,
                    totalCachedMp3FilesOnDisk = repository.getCachedAyahCount()
                )
            }
            refreshAuthenticVerses()
        }
    }

    // Wizard navigation
    fun setWizardStep(step: WizardStep) {
        if (step != WizardStep.PREVIEW) {
            stopPreviewPlayback()
        }
        _uiState.update { it.copy(currentWizardStep = step) }
        if (step == WizardStep.PREVIEW) {
            preparePreviewAndCacheAudio()
        }
    }

    fun nextWizardStep() {
        val next = when (_uiState.value.currentWizardStep) {
            WizardStep.CONTENT -> WizardStep.DESIGN
            WizardStep.DESIGN -> WizardStep.PREVIEW
            WizardStep.PREVIEW -> WizardStep.PREVIEW
        }
        setWizardStep(next)
    }

    fun previousWizardStep() {
        val prev = when (_uiState.value.currentWizardStep) {
            WizardStep.CONTENT -> WizardStep.CONTENT
            WizardStep.DESIGN -> WizardStep.CONTENT
            WizardStep.PREVIEW -> WizardStep.DESIGN
        }
        setWizardStep(prev)
    }

    fun setOnboardingPage(page: Int) {
        _uiState.update { it.copy(onboardingPage = page.coerceIn(0, 2)) }
    }

    fun skipOrFinishOnboarding() {
        _uiState.update { it.copy(showOnboarding = false) }
    }

    fun reopenOnboarding() {
        stopPreviewPlayback()
        _uiState.update { it.copy(showOnboarding = true, onboardingPage = 0) }
    }

    fun toggleLanguageCode() {
        _uiState.update {
            it.copy(uiLanguageCode = if (it.uiLanguageCode == "EN") "AR" else "EN")
        }
    }

    fun selectReciter(reciter: ReciterOption) {
        stopPreviewPlayback()
        _uiState.update { it.copy(selectedReciter = reciter, audioDownloadError = null) }
        refreshAuthenticVerses()
    }

    fun selectSurah(surah: SurahInfo) {
        stopPreviewPlayback()
        val defaultCount = 3.coerceAtMost(surah.versesCount)
        _uiState.update {
            it.copy(
                selectedSurah = surah,
                startAyah = 1,
                numberOfAyahs = defaultCount,
                audioDownloadError = null
            )
        }
        refreshAuthenticVerses()
    }

    fun incrementStartAyah() {
        val state = _uiState.value
        val maxStart = state.selectedSurah.versesCount
        if (state.startAyah < maxStart) {
            val newStart = state.startAyah + 1
            val maxAllowedCount = (maxStart - newStart + 1).coerceAtLeast(1)
            val newCount = state.numberOfAyahs.coerceAtMost(maxAllowedCount)
            _uiState.update { it.copy(startAyah = newStart, numberOfAyahs = newCount, audioDownloadError = null) }
            refreshAuthenticVerses()
        }
    }

    fun decrementStartAyah() {
        val state = _uiState.value
        if (state.startAyah > 1) {
            _uiState.update { it.copy(startAyah = state.startAyah - 1, audioDownloadError = null) }
            refreshAuthenticVerses()
        }
    }

    fun incrementNumberOfAyahs() {
        val state = _uiState.value
        val maxRemaining = state.selectedSurah.versesCount - state.startAyah + 1
        if (state.numberOfAyahs < maxRemaining && state.numberOfAyahs < 10) {
            _uiState.update { it.copy(numberOfAyahs = state.numberOfAyahs + 1, audioDownloadError = null) }
            refreshAuthenticVerses()
        }
    }

    fun decrementNumberOfAyahs() {
        val state = _uiState.value
        if (state.numberOfAyahs > 1) {
            _uiState.update { it.copy(numberOfAyahs = state.numberOfAyahs - 1, audioDownloadError = null) }
            refreshAuthenticVerses()
        }
    }

    fun selectTranslation(option: TranslationLanguageOption) {
        _uiState.update {
            it.copy(
                selectedTranslation = option,
                textStyleConfig = it.textStyleConfig.copy(showEnglishTranslation = option.id != 0)
            )
        }
        refreshAuthenticVerses()
    }

    fun toggleTranslationVisibility(enabled: Boolean) {
        _uiState.update {
            it.copy(
                textStyleConfig = it.textStyleConfig.copy(showEnglishTranslation = enabled)
            )
        }
    }

    fun toggleMushafPage(enabled: Boolean) {
        _uiState.update { it.copy(showMushafPage = enabled) }
    }

    // Pexels Video Search & Background Selection
    fun openBackgroundModal(show: Boolean) {
        _uiState.update { it.copy(showBackgroundModal = show) }
    }

    fun searchPexelsBackgrounds(query: String) {
        _uiState.update { it.copy(pexelsSearchQuery = query, isSearchingPexels = true) }
        pexelsSearchJob?.cancel()
        pexelsSearchJob = viewModelScope.launch {
            delay(220L)
            val results = pexelsRepository.searchPexelsVideos(query)
            _uiState.update {
                it.copy(
                    pexelsVideos = results,
                    isSearchingPexels = false
                )
            }
        }
    }

    fun selectBackgroundScene(scene: BackgroundScene) {
        _uiState.update {
            it.copy(
                selectedBackground = scene,
                hasExplicitlyChosenBackground = true,
                showBackgroundModal = false
            )
        }
    }

    fun addCustomGalleryBackground(uri: Uri) {
        val customScene = BackgroundScene(
            videoId = 500 + _uiState.value.userGalleryVideos.size,
            title = "Gallery Media #${_uiState.value.userGalleryVideos.size + 1}",
            category = "Your Videos",
            durationLabel = "Custom",
            videographer = "Device Gallery",
            primaryHex = 0xFF142A34,
            secondaryHex = 0xFF2A9D8F,
            accentHex = 0xFFE9C46A,
            customUri = uri
        )
        _uiState.update {
            it.copy(
                userGalleryVideos = listOf(customScene) + it.userGalleryVideos,
                selectedBackground = customScene,
                hasExplicitlyChosenBackground = true,
                showBackgroundModal = false
            )
        }
    }

    // Text Style BottomSheet
    fun openTextStyleSheet(show: Boolean) {
        _uiState.update { it.copy(showTextStyleSheet = show) }
    }

    fun updateTextStyle(transform: (TextStyleConfig) -> TextStyleConfig) {
        _uiState.update { it.copy(textStyleConfig = transform(it.textStyleConfig)) }
    }

    // Video Filters BottomSheet (Sliders for Brightness, Contrast, Saturation, Blur)
    fun openVideoFiltersSheet(show: Boolean) {
        _uiState.update { it.copy(showVideoFiltersSheet = show) }
    }

    fun updateVideoFilterConfig(transform: (VideoFilterConfig) -> VideoFilterConfig) {
        _uiState.update { it.copy(videoFilterConfig = transform(it.videoFilterConfig)) }
    }

    fun resetVideoFilters() {
        _uiState.update { it.copy(videoFilterConfig = VideoFilterConfig()) }
    }

    fun openFlutterCodeSheet(show: Boolean) {
        _uiState.update { it.copy(showFlutterCodeSheet = show) }
    }

    fun applyQuranComMediaUrl(url: String) {
        try {
            val uri = Uri.parse(url.trim())
            val surahId = uri.getQueryParameter("surah")?.toIntOrNull() ?: 2
            val verseFrom = uri.getQueryParameter("verseFrom")?.toIntOrNull() ?: 1
            val verseTo = uri.getQueryParameter("verseTo")?.toIntOrNull() ?: verseFrom
            val reciterId = uri.getQueryParameter("mediaReciter")?.toIntOrNull() ?: 13
            val transId = uri.getQueryParameter("mediaTranslations")?.toIntOrNull() ?: 131
            val bgColor = uri.getQueryParameter("backgroundColor") ?: "#000000"
            val opacity = uri.getQueryParameter("opacity")?.toFloatOrNull() ?: 0.18f
            val borderColor = uri.getQueryParameter("borderColor") ?: "#E9C46A"
            val borderSize = uri.getQueryParameter("borderSize")?.toIntOrNull() ?: 0
            val fontColor = uri.getQueryParameter("fontColor") ?: "#ffffff"
            val videoId = uri.getQueryParameter("videoId")?.toIntOrNull() ?: 14
            val quranScale = uri.getQueryParameter("quranTextFontScale")?.toIntOrNull() ?: 4
            val transScale = uri.getQueryParameter("translationFontScale")?.toIntOrNull() ?: 3

            val matchedSurah = _uiState.value.chapters.find { it.id == surahId }
                ?: SurahInfo(surahId, "البقرة", "Al-Baqarah", "The Cow", 286, 2)
            val matchedReciter = _uiState.value.reciters.find { it.id == reciterId }
                ?: _uiState.value.reciters.first()
            val matchedTrans = _uiState.value.translationOptions.find { it.id == transId }
                ?: _uiState.value.translationOptions.first()
            val matchedBg = _uiState.value.pexelsVideos.find { it.videoId == videoId }
                ?: _uiState.value.pexelsVideos.first()

            val count = (verseTo - verseFrom + 1).coerceIn(1, 10)
            _uiState.update {
                it.copy(
                    selectedSurah = matchedSurah,
                    startAyah = verseFrom.coerceIn(1, matchedSurah.versesCount),
                    numberOfAyahs = count,
                    selectedReciter = matchedReciter,
                    selectedTranslation = matchedTrans,
                    selectedBackground = matchedBg,
                    hasExplicitlyChosenBackground = true,
                    textStyleConfig = it.textStyleConfig.copy(
                        backgroundColorHex = bgColor,
                        backgroundOpacity = opacity.coerceIn(0f, 0.75f),
                        borderColorHex = borderColor,
                        borderSize = borderSize.coerceIn(0, 10),
                        textColorHex = fontColor,
                        quranTextFontScale = quranScale.coerceIn(1, 10),
                        arabicFontSizeSp = (18f + quranScale * 3.2f),
                        translationFontScale = transScale.coerceIn(1, 10),
                        translationFontSizeSp = (10f + transScale * 1.6f)
                    )
                )
            }
            refreshAuthenticVerses()
        } catch (_: Exception) {
        }
    }

    /**
     * Triggered when the user taps "Try Again" on the friendly Serene Light error screen.
     * Re-runs both authentic verse fetching and the multi-CDN fallback audio downloader.
     */
    fun retryAudioDownloadWithFallbacks() {
        _uiState.update {
            it.copy(
                verseLoadError = null,
                audioDownloadError = null,
                exportState = ExportUiState.Idle
            )
        }
        if (_uiState.value.currentWizardStep == WizardStep.PREVIEW) {
            preparePreviewAndCacheAudio()
        } else {
            refreshAuthenticVerses()
        }
    }

    fun refreshAuthenticVerses() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingVerses = true,
                    verseLoadError = null,
                    audioDownloadError = null
                )
            }
            val res = repository.fetchAuthenticVerses(
                surahId = state.selectedSurah.id,
                startAyah = state.startAyah,
                numberOfAyahs = state.numberOfAyahs,
                translationId = state.selectedTranslation.id,
                reciter = state.selectedReciter
            )
            res.fold(
                onSuccess = { verses ->
                    val cachedCount = verses.count { it.isCachedLocally }
                    _uiState.update {
                        it.copy(
                            authenticAyahs = verses,
                            isLoadingVerses = false,
                            verseLoadError = null,
                            audioCacheStatusMessage = "$cachedCount/${verses.size} Ayah MP3s in local cache",
                            totalCachedMp3FilesOnDisk = repository.getCachedAyahCount()
                        )
                    }
                },
                onFailure = {
                    // Never show raw Exception strings; always show the human-friendly message
                    _uiState.update {
                        it.copy(
                            isLoadingVerses = false,
                            verseLoadError = FRIENDLY_AUDIO_ERROR_MESSAGE_AR
                        )
                    }
                }
            )
        }
    }

    private fun preparePreviewAndCacheAudio() {
        audioDownloadJob?.cancel()
        audioDownloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    audioDownloadError = null,
                    verseLoadError = null
                )
            }

            if (_uiState.value.authenticAyahs.isEmpty()) {
                val state = _uiState.value
                _uiState.update { it.copy(isLoadingVerses = true) }
                val res = repository.fetchAuthenticVerses(
                    surahId = state.selectedSurah.id,
                    startAyah = state.startAyah,
                    numberOfAyahs = state.numberOfAyahs,
                    translationId = state.selectedTranslation.id,
                    reciter = state.selectedReciter
                )
                res.fold(
                    onSuccess = { verses ->
                        _uiState.update {
                            it.copy(
                                authenticAyahs = verses,
                                isLoadingVerses = false,
                                verseLoadError = null
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isLoadingVerses = false,
                                verseLoadError = FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
                                audioDownloadError = FRIENDLY_AUDIO_ERROR_MESSAGE_AR
                            )
                        }
                        return@launch
                    }
                )
            }

            val currentAyahs = _uiState.value.authenticAyahs
            if (currentAyahs.isNotEmpty()) {
                val needsNetworkDownload = currentAyahs.any { !it.isCachedLocally }
                if (needsNetworkDownload) {
                    val firstAyahNum = currentAyahs.first().ayahNumber
                    _uiState.update {
                        it.copy(
                            isDownloadingAudio = true,
                            audioDownloadProgressPercent = 5,
                            audioDownloadProgressLabel = "Downloading Ayah $firstAyahNum... 5%"
                        )
                    }
                }

                val batchResult = repository.ensureAyahRangeAudioCachedWithProgress(
                    ayahs = currentAyahs,
                    reciter = _uiState.value.selectedReciter
                ) { ayahNumber, _, _, percent, statusText ->
                    _uiState.update {
                        it.copy(
                            isDownloadingAudio = percent < 100,
                            audioDownloadProgressPercent = percent,
                            audioDownloadProgressLabel = statusText,
                            audioCacheStatusMessage = "Downloading Ayah $ayahNumber... $percent%"
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        authenticAyahs = batchResult.ayahs,
                        isDownloadingAudio = false,
                        audioDownloadProgressPercent = 100,
                        audioDownloadError = if (batchResult.allSucceeded) null else FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
                        totalCachedMp3FilesOnDisk = repository.getCachedAyahCount()
                    )
                }

                if (batchResult.allSucceeded) {
                    startSequentialPreviewPlayback(startIndex = 0)
                }
            }
        }
    }

    fun togglePreviewPlayback() {
        if (_uiState.value.isPlayingPreview) {
            stopPreviewPlayback()
        } else {
            if (_uiState.value.audioDownloadError != null) {
                retryAudioDownloadWithFallbacks()
            } else {
                startSequentialPreviewPlayback(_uiState.value.activePreviewAyahIndex)
            }
        }
    }

    fun stepPreviewAyah(delta: Int) {
        val list = _uiState.value.authenticAyahs
        if (list.isEmpty()) return
        val nextIdx = (_uiState.value.activePreviewAyahIndex + delta).coerceIn(0, list.lastIndex)
        startSequentialPreviewPlayback(nextIdx)
    }

    private fun startSequentialPreviewPlayback(startIndex: Int) {
        stopPreviewPlayback()
        val ayahs = _uiState.value.authenticAyahs
        if (ayahs.isEmpty()) return

        playbackJob = viewModelScope.launch {
            _uiState.update { it.copy(isPlayingPreview = true, audioDownloadError = null) }

            for (idx in startIndex until ayahs.size) {
                val ayah = ayahs[idx]
                _uiState.update { it.copy(activePreviewAyahIndex = idx) }

                playSingleAyahAudioWithFallback(ayah)

                val style = _uiState.value.textStyleConfig
                if (style.enableTtsVoiceover && style.showEnglishTranslation && ayah.translationText.isNotBlank()) {
                    val engineBadge = ttsService.speakTranslationPreview(ayah.translationText)
                    _uiState.update { it.copy(activeTtsEngineBadge = engineBadge) }
                }
            }

            _uiState.update { it.copy(isPlayingPreview = false, activePreviewAyahIndex = 0) }
        }
    }

    /**
     * Plays a single Ayah audio file from local cache first, or falls back across
     * EveryAyah and Quran.com CDN URLs if streaming is needed.
     */
    private suspend fun playSingleAyahAudioWithFallback(ayah: AuthenticAyah) {
        val sourcesToTry = mutableListOf<String>()
        val localFile = ayah.localCachedFile
        if (localFile != null && localFile.exists() && localFile.length() > 256L) {
            sourcesToTry.add(localFile.absolutePath)
        }
        sourcesToTry.add(ayah.audioUrl)
        sourcesToTry.addAll(ayah.fallbackAudioUrls)

        for (source in sourcesToTry.distinct()) {
            val playedOk = tryPlayAudioSource(source, ayah.durationMs)
            if (playedOk) return
        }

        // Graceful timed fallback if device audio output is unavailable
        delay(ayah.durationMs.coerceIn(2500L, 6000L))
    }

    private suspend fun tryPlayAudioSource(dataSource: String, expectedDurationMs: Long): Boolean {
        var finished = false
        var hadError = false
        return try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(dataSource)
            player.setOnCompletionListener { finished = true }
            player.setOnErrorListener { _, _, _ ->
                hadError = true
                finished = true
                true
            }
            player.prepareAsync()
            var waitedMs = 0L
            val maxWaitMs = (expectedDurationMs + 4500L).coerceAtMost(20000L)
            player.setOnPreparedListener { mp ->
                mp.start()
            }
            while (!finished && waitedMs < maxWaitMs) {
                delay(120L)
                waitedMs += 120L
            }
            !hadError
        } catch (_: Exception) {
            false
        } finally {
            releaseMediaPlayer()
        }
    }

    fun stopPreviewPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        releaseMediaPlayer()
        ttsService.stop()
        _uiState.update { it.copy(isPlayingPreview = false) }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    fun startMp4Export() {
        val state = _uiState.value
        if (state.authenticAyahs.isEmpty()) {
            retryAudioDownloadWithFallbacks()
            return
        }

        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val firstAyahNum = state.authenticAyahs.first().ayahNumber
            _uiState.update {
                it.copy(
                    audioDownloadError = null,
                    exportState = ExportUiState.Processing(
                        progress = 0.04f,
                        statusMessage = "Downloading Ayah $firstAyahNum... 4%"
                    )
                )
            }
            try {
                val batchResult = repository.ensureAyahRangeAudioCachedWithProgress(
                    ayahs = state.authenticAyahs,
                    reciter = state.selectedReciter
                ) { _, _, _, percent, statusText ->
                    val scaledProgress = (0.04f + (percent / 100f) * 0.20f).coerceIn(0.04f, 0.24f)
                    _uiState.update {
                        it.copy(
                            exportState = ExportUiState.Processing(
                                progress = scaledProgress,
                                statusMessage = statusText
                            )
                        )
                    }
                }

                if (!batchResult.allSucceeded) {
                    _uiState.update {
                        it.copy(
                            audioDownloadError = FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
                            exportState = ExportUiState.Error(
                                arabicMessage = FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
                                englishMessage = FRIENDLY_AUDIO_ERROR_MESSAGE_EN
                            )
                        )
                    }
                    return@launch
                }

                val result = ReelVideoExportEngine.exportReelMp4(
                    context = getApplication(),
                    surah = state.selectedSurah,
                    reciter = state.selectedReciter,
                    ayahs = batchResult.ayahs,
                    backgroundScene = state.selectedBackground,
                    textStyle = state.textStyleConfig,
                    mushafPageMode = state.showMushafPage,
                    videoFilter = state.videoFilterConfig
                ) { progress, msg ->
                    _uiState.update {
                        it.copy(exportState = ExportUiState.Processing(progress, msg))
                    }
                }

                _uiState.update { it.copy(exportState = ExportUiState.Completed(result)) }
            } catch (_: Exception) {
                // Never show raw Exception strings to the user
                _uiState.update {
                    it.copy(
                        exportState = ExportUiState.Error(
                            arabicMessage = FRIENDLY_AUDIO_ERROR_MESSAGE_AR,
                            englishMessage = FRIENDLY_AUDIO_ERROR_MESSAGE_EN
                        )
                    )
                }
            }
        }
    }

    fun resetExportState() {
        _uiState.update { it.copy(exportState = ExportUiState.Idle, audioDownloadError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreviewPlayback()
        ttsService.shutdown()
    }
}
