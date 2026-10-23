# Quran Reels — 9:16 Vertical Quran Video Maker

A responsive Material 3 application for creating 9:16 vertical Quran Reels (1080×1920 at 30fps) with **100% authentic Uthmani script** fetched live from the **Quran.com API v4 (`api.quran.com/api/v4`)**, granular **Ayah-by-Ayah MP3 downloading & local caching** from **EveryAyah**, **Mushaf Page highlight mode** (recited Ayah highlighted in red), **Gemini TTS (`gemini-3.8-flash-tts`) + Offline Native Android TTS**, and local on-device video rendering using **Dart Isolates** and **`ffmpeg_kit_flutter`**.

---

## Features & UI Screens

1. **Color Palette & Responsive Web Viewport (`maxWidth: 480px`)**:
   - **Background**: Deep Sanctuary Black-Green (`#050B08`)
   - **Cards Background**: Dark Emerald Green (`#0E241D`) with `BorderRadius.circular(12)`
   - **Primary Accent**: Burnished Gold (`#D4AF37`) for icons, `"Quran Reels"` title, `"7 Ayah"` badge, active switches, steppers, and pagination dots
   - **Primary CTA Buttons**: Rich Emerald Green (`#14533E`)
2. **Onboarding Carousel (3 Screens)**:
   - Top-right `Skip` button, centered Islamic vector illustrations, bold white title & light gray subtitle, Gold/Dark-Green pagination dots, and a large Emerald Green `Next` / `Get Started` button.
3. **Main "Create Reel" Screen (Scrollable `ListView` of 8 Rounded Cards)**:
   - **Header**: `"Quran Reels"` (Gold, left), `"EN"` language globe button + Info (`ⓘ`) button (right).
   - **Card 1 (`Select Reciter`)**: Mic icon + dropdown (`Mishary Rashid Alafasy`, `Saad Al-Ghamdi`, `AbdulBaset AbdulSamad`, `Mahmoud Khalil Al-Husary`, `Abdurrahmaan As-Sudais`).
   - **Card 2 (`Select Surah`)**: Number icon + searchable dropdown for all 114 Surahs (`1 الفاتحة (Al-Fatiha)`, `2 البقرة (Al-Baqarah)`, etc.).
   - **Card 3 (`Ayah Range`)**: Dynamic `"7 Ayah"` Gold badge + custom `-` / `+` steppers for **Start Ayah** and **Number of Ayahs**.
   - **Card 4 (`Translation Language`)**: Translate icon + dropdown (`English`, `French`, `Urdu`, `Indonesian`, `Arabic Only`) + optional AI/Native TTS narration.
   - **Card 5 (`Mushaf page`)**: Book icon, `"Show the Quran page, recited ayah in red"`, and Gold toggle switch.
   - **Card 6 (`Select Background`)**: Video icon + `"Tap to choose a b..."` opening the 3-column Background Selection Modal (`Your Videos` & `Browse` tabs + search bar).
   - **Card 7 (`Text Style`)**: Font icon + `"Amiri / Roboto"` opening the Text Styling BottomSheet (Arabic Font Size slider, Text & Background Color + Opacity, Top/Center/Bottom position, Translation switch).
   - **Card 8 (`Video Filters`)**: Filter icon + `"Off"` opening the Video Filters BottomSheet (`Off`, `Warm Gold`, `Dark Vignette`, `Soft Blur`, `Cinematic Sepia`, `Emerald Glow`).
   - **Bottom CTA**: Full-width Emerald Green `▶ Generate` button + `"Your reel will be in 9:16 format, perfect for social media."` footer.

---

## Building the APK (Flutter & Gradle Instructions)

### Prerequisites
- Flutter SDK `>= 3.22.0` (Dart `>= 3.4.0`)
- Android Studio / Android SDK (API 34–36, `minSdkVersion 24` required by `ffmpeg_kit_flutter_full_gpl`)
- Java 17

### Step-by-Step Build Commands (Flutter Project in `/flutter_quran_reels`)

```bash
cd flutter_quran_reels

# 1. Fetch Dart & Flutter packages
flutter pub get

# 2. Run in Chrome to test the simulated 480px Responsive Web UI (Phase 1)
flutter run -d chrome

# 3. Build the Release Android APK (arm64-v8a / universal)
flutter build apk --release --split-per-abi
```

The compiled APKs will be generated at:
`flutter_quran_reels/build/app/outputs/flutter-apk/app-arm64-v8a-release.apk`
