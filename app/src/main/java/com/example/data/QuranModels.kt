package com.example.data

import android.net.Uri
import java.io.File
import kotlin.math.roundToInt

enum class WizardStep(val stepNumber: Int, val title: String, val subtitle: String) {
    CONTENT(1, "Content", "Reciter, Surah & Ayahs"),
    DESIGN(2, "Design", "Background, Text & Filters"),
    PREVIEW(3, "Preview", "9:16 Live Preview & Export")
}

/**
 * Converts standard Western digits (0-9) into Eastern Arabic-Indic digits (٠-٩)
 * so Uthmani end-of-ayah markers render authentically inside ﴿١﴾ without BiDi reversal.
 */
fun Int.toArabicIndicDigits(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

data class ReciterOption(
    val id: Int,
    val name: String,
    val arabicName: String,
    val style: String,
    val everyAyahSubfolder: String,
    val quranComCdnPath: String
) {
    companion object {
        val ALL = listOf(
            ReciterOption(
                id = 7,
                name = "Mishary Rashid Alafasy",
                arabicName = "مشاري راشد العفاسي",
                style = "Murattal • 128kbps",
                everyAyahSubfolder = "Alafasy_128kbps",
                quranComCdnPath = "Alafasy/mp3"
            ),
            ReciterOption(
                id = 13,
                name = "Saad Al-Ghamdi",
                arabicName = "سعد الغامدي",
                style = "Murattal • 40kbps",
                everyAyahSubfolder = "Ghamadi_40kbps",
                quranComCdnPath = "Ghamadi/mp3"
            ),
            ReciterOption(
                id = 2,
                name = "AbdulBaset AbdulSamad",
                arabicName = "عبد الباسط عبد الصمد",
                style = "Murattal • 192kbps",
                everyAyahSubfolder = "Abdul_Basit_Murattal_192kbps",
                quranComCdnPath = "AbdulBaset/Mujawwad/mp3"
            ),
            ReciterOption(
                id = 6,
                name = "Mahmoud Khalil Al-Husary",
                arabicName = "محمود خليل الحصري",
                style = "Murattal • 128kbps",
                everyAyahSubfolder = "Husary_128kbps",
                quranComCdnPath = "Husary/mp3"
            ),
            ReciterOption(
                id = 3,
                name = "Abdurrahmaan As-Sudais",
                arabicName = "عبدالرحمن السديس",
                style = "Murattal • 192kbps",
                everyAyahSubfolder = "Abdurrahmaan_As-Sudais_192kbps",
                quranComCdnPath = "Sudais/mp3"
            )
        )
    }
}

data class SurahInfo(
    val id: Int,
    val nameArabic: String,
    val nameSimple: String,
    val nameTranslated: String,
    val versesCount: Int,
    val mushafPageNumber: Int = 1
) {
    val displayLabel: String
        get() = "$id $nameArabic ($nameSimple)"
}

data class TranslationLanguageOption(
    val id: Int,
    val languageName: String,
    val translatorName: String,
    val languageCode: String
) {
    companion object {
        val ALL = listOf(
            TranslationLanguageOption(
                id = 131,
                languageName = "English",
                translatorName = "Dr. Mustafa Khattab (The Clear Quran)",
                languageCode = "en"
            ),
            TranslationLanguageOption(
                id = 20,
                languageName = "English (Saheeh)",
                translatorName = "Saheeh International",
                languageCode = "en"
            ),
            TranslationLanguageOption(
                id = 136,
                languageName = "French",
                translatorName = "Montada Islamic Foundation",
                languageCode = "fr"
            ),
            TranslationLanguageOption(
                id = 158,
                languageName = "Urdu",
                translatorName = "Bayan-ul-Quran",
                languageCode = "ur"
            ),
            TranslationLanguageOption(
                id = 33,
                languageName = "Indonesian",
                translatorName = "Indonesian Ministry of Religious Affairs",
                languageCode = "id"
            ),
            TranslationLanguageOption(
                id = 0,
                languageName = "None (Arabic Only)",
                translatorName = "Uthmani Script Only",
                languageCode = "ar"
            )
        )
    }
}

data class AuthenticAyah(
    val surahId: Int,
    val ayahNumber: Int,
    val verseKey: String,
    val textUthmani: String,
    val translationText: String,
    val everyAyahFileName: String,
    val audioUrl: String,
    val fallbackAudioUrls: List<String> = emptyList(),
    val localCachedFile: File? = null,
    val isCachedLocally: Boolean = false,
    val durationMs: Long = 5200L,
    val pageNumber: Int = 1
) {
    /**
     * Returns the authentic Uthmani verse text followed by a non-breaking space and
     * an RTL-isolated ornate Ayah number marker (e.g., ﴿١﴾) using Eastern Arabic-Indic digits
     * so brackets never overlap or flip direction.
     */
    fun formattedUthmaniWithNumber(): String {
        val cleaned = textUthmani
            .replace(Regex("[\\uFD3E\\uFD3F\\u06DD0-9٠-٩]+\\s*$"), "")
            .trim()
        val arabicNum = ayahNumber.toArabicIndicDigits()
        return "\u200F$cleaned\u00A0\uFD3F$arabicNum\uFD3E"
    }
}

enum class BackgroundThemeType {
    DEEP_COSMOS,
    OCEAN_NIGHT,
    STARRY_HORIZON,
    AURORA_TEAL,
    MOUNTAIN_MIST,
    RAIN_DROPLETS,
    DESERT_TWILIGHT,
    CLOUDS_DRIFT,
    WATERFALL_ZEN,
    FOREST_FOG,
    GALAXY_NEBULA,
    MINIMAL_NAVY
}

data class BackgroundScene(
    val videoId: Int,
    val title: String,
    val category: String,
    val durationLabel: String,
    val videographer: String = "Pexels Studio",
    val themeType: BackgroundThemeType = BackgroundThemeType.DEEP_COSMOS,
    val primaryHex: Long = 0xFF142A34,
    val secondaryHex: Long = 0xFF1D4E56,
    val accentHex: Long = 0xFFE9C46A,
    val thumbnailUrl: String? = null,
    val videoStreamUrl: String? = null,
    val customUri: Uri? = null
) {
    companion object {
        val PEXELS_CURATED_PACK = listOf(
            BackgroundScene(
                videoId = 14,
                title = "Starlit Night Sky",
                category = "Night Sky",
                durationLabel = "0:18",
                videographer = "Pexels • Ruvim Miksanskiy",
                themeType = BackgroundThemeType.DEEP_COSMOS,
                primaryHex = 0xFF0F2027,
                secondaryHex = 0xFF203A43,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/856973/free-video-856973.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 101,
                title = "Calm Emerald Ocean Waves",
                category = "Ocean",
                durationLabel = "0:24",
                videographer = "Pexels • Thierry Fillieul",
                themeType = BackgroundThemeType.OCEAN_NIGHT,
                primaryHex = 0xFF133337,
                secondaryHex = 0xFF2A9D8F,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/1093662/free-video-1093662.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 102,
                title = "Northern Aurora Borealis",
                category = "Sky",
                durationLabel = "0:20",
                videographer = "Pexels • Distill",
                themeType = BackgroundThemeType.AURORA_TEAL,
                primaryHex = 0xFF0F292F,
                secondaryHex = 0xFF1E5F5B,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/854100/free-video-854100.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 103,
                title = "Misty Mountain Peaks",
                category = "Nature",
                durationLabel = "0:15",
                videographer = "Pexels • Pixabay",
                themeType = BackgroundThemeType.MOUNTAIN_MIST,
                primaryHex = 0xFF1B3039,
                secondaryHex = 0xFF2C5364,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/857195/free-video-857195.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 104,
                title = "Serene Rain on Glass",
                category = "Rain",
                durationLabel = "0:22",
                videographer = "Pexels • VladBagacian",
                themeType = BackgroundThemeType.RAIN_DROPLETS,
                primaryHex = 0xFF16262E,
                secondaryHex = 0xFF264653,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/2491284/free-video-2491284.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 105,
                title = "Golden Desert Dunes",
                category = "Desert",
                durationLabel = "0:19",
                videographer = "Pexels • Taryn Elliott",
                themeType = BackgroundThemeType.DESERT_TWILIGHT,
                primaryHex = 0xFF264653,
                secondaryHex = 0xFF6B5B3E,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/3015510/free-video-3015510.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 106,
                title = "Soft Horizon Clouds Drift",
                category = "Clouds",
                durationLabel = "0:30",
                videographer = "Pexels • Pressmaster",
                themeType = BackgroundThemeType.CLOUDS_DRIFT,
                primaryHex = 0xFF1D3540,
                secondaryHex = 0xFF2A9D8F,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/855289/free-video-855289.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 107,
                title = "Deep Space Nebula",
                category = "Space",
                durationLabel = "0:25",
                videographer = "Pexels • Ingrid North",
                themeType = BackgroundThemeType.GALAXY_NEBULA,
                primaryHex = 0xFF111D24,
                secondaryHex = 0xFF264653,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/856309/free-video-856309.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 108,
                title = "Tranquil Waterfall Mist",
                category = "Nature",
                durationLabel = "0:16",
                videographer = "Pexels • Kelly",
                themeType = BackgroundThemeType.WATERFALL_ZEN,
                primaryHex = 0xFF163832,
                secondaryHex = 0xFF2A9D8F,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/1448735/free-video-1448735.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 109,
                title = "Morning Pine Forest",
                category = "Forest",
                durationLabel = "0:21",
                videographer = "Pexels • ehl",
                themeType = BackgroundThemeType.FOREST_FOG,
                primaryHex = 0xFF1A3A3A,
                secondaryHex = 0xFF264653,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/1409899/free-video-1409899.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 110,
                title = "Horizon Starlight Trail",
                category = "Night Sky",
                durationLabel = "0:28",
                videographer = "Pexels • eberhard grossgasteiger",
                themeType = BackgroundThemeType.STARRY_HORIZON,
                primaryHex = 0xFF142834,
                secondaryHex = 0xFF264653,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = "https://images.pexels.com/videos/854671/free-video-854671.jpg?auto=compress&cs=tinysrgb&w=600"
            ),
            BackgroundScene(
                videoId = 111,
                title = "Serene Teal & Gold Gradient",
                category = "Minimal",
                durationLabel = "Loop",
                videographer = "Ayah Studio",
                themeType = BackgroundThemeType.MINIMAL_NAVY,
                primaryHex = 0xFF264653,
                secondaryHex = 0xFF2A9D8F,
                accentHex = 0xFFE9C46A,
                thumbnailUrl = null
            )
        )
    }
}

enum class TextPosition(val label: String) {
    TOP("Top"),
    CENTER("Center"),
    BOTTOM("Bottom")
}

enum class ArabicFontOption(val label: String, val styleKey: String) {
    AMIRI("Amiri Quran", "amiri_quran"),
    QPC_HAFS("KFGQPC Uthmani", "qpc_uthmani_hafs"),
    SCHEHERAZADE("Classic Naskh", "classic_naskh")
}

enum class EnglishFontOption(val label: String, val styleKey: String) {
    ROBOTO("Roboto Sans", "roboto_sans"),
    SERIF("Editorial Serif", "editorial_serif"),
    MONO("Modern Mono", "modern_mono")
}

data class TextStyleConfig(
    val arabicFont: ArabicFontOption = ArabicFontOption.AMIRI,
    val englishFont: EnglishFontOption = EnglishFontOption.ROBOTO,
    val arabicFontSizeSp: Float = 30f,
    val translationFontSizeSp: Float = 14f,
    val quranTextFontScale: Int = 4,
    val translationFontScale: Int = 3,
    val textColorHex: String = "#FFFFFF",
    val translationColorHex: String = "#F8F9FA",
    val backgroundColorHex: String = "#000000",
    val backgroundOpacity: Float = 0.18f,
    val borderColorHex: String = "#E9C46A",
    val borderSize: Int = 0,
    val textPosition: TextPosition = TextPosition.CENTER,
    val showEnglishTranslation: Boolean = true,
    val enableTtsVoiceover: Boolean = false,
    val ttsVoiceName: String = "Kore"
) {
    val fontFamilyStyle: String
        get() = "${arabicFont.label.substringBefore(" ")} / ${englishFont.label.substringBefore(" ")}"

    val quranTextFontStyleKey: String
        get() = arabicFont.styleKey
}

/**
 * Video Filters with real-time Sliders for Brightness, Contrast, Saturation, and Blur
 */
data class VideoFilterConfig(
    val brightness: Float = 0f,     // Range: -100f..100f (0f = normal)
    val contrast: Float = 1.0f,     // Range: 0.5f..2.0f  (1.0f = 100% normal)
    val saturation: Float = 1.0f,   // Range: 0.0f..2.0f  (1.0f = 100% normal, 0f = B&W)
    val blurRadius: Float = 0f      // Range: 0f..25f     (0f = sharp)
) {
    val isModified: Boolean
        get() = brightness.roundToInt() != 0 ||
            (contrast * 100).roundToInt() != 100 ||
            (saturation * 100).roundToInt() != 100 ||
            blurRadius.roundToInt() != 0

    val summaryLabel: String
        get() = if (!isModified) {
            "Normal (100%)"
        } else {
            "B:${brightness.roundToInt()} • C:${(contrast * 100).roundToInt()}% • S:${(saturation * 100).roundToInt()}% • Blur:${blurRadius.roundToInt()}px"
        }
}
