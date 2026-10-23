# Quran Reels — Flutter Material 3 Web UI, Onboarding & `ffmpeg_kit_flutter` Studio

Quran Reels is a **Flutter (Dart)** Material 3 application styled in a deep dark-green/black canvas (`#050B08`), dark emerald cards (`#0E241D`), and burnished gold accents (`#D4AF37`), constrained within a **480px centered mobile-web viewport** for responsive testing. It implements the exact **3-Screen Onboarding Flow** and the **8-Card Scrollable "Quran Reels" Main Screen** from your reference, paired with **Background Selection (`Your Videos` / `Browse` 3-column grid)**, **Text Style (`Amiri / Roboto`) BottomSheet**, **Video Filters**, **100% authentic Quran.com API v4 script**, **Ayah-by-Ayah MP3 caching (`SSSAAA.mp3`)**, and **Dart Isolate + `ffmpeg_kit_flutter`** 1080×1920 @ 30fps MP4 generation.

## User Review & Critical Decisions

> [!IMPORTANT]
> To start generating the code immediately, click the **Proceed** button on the plan approval bar. Every visual detail from your specification is locked in below:

- **1. Color Palette & Responsive Web Viewport (`maxWidth: 480px`)**:
  - **App Background**: Very dark green/black (`#050B08`).
  - **Cards Background**: Dark Emerald Green (`#0E241D`) with `BorderRadius.circular(12)`.
  - **Primary Accent**: Gold/Yellow (`#D4AF37`) for leading icons, active text, `"Quran Reels"` header title, `"7 Ayah"` count badge, stepper values, active switch states, and pagination dots.
  - **Primary Action Button**: Rich Emerald Green (`#14533E` / `#1B6B50`) with crisp white label and Play icon (`▶ Generate`, `Next`, `Get Started`).
  - **Typography**: White (`#FFFFFF`) bold headings and Light Gray (`#9EABA6`) subtitles.
- **2. Onboarding Flow (3 Screens)**:
  - Full-screen `#050B08` dark background with top-right **`Skip`** button.
  - Centered thematic illustration on each page:
    1. *Create Beautiful Quran Reels* — Share authentic verses with serene backgrounds in 9:16 vertical format.
    2. *World-Renowned Reciters & Mushaf Mode* — Choose your favorite Qari with synchronized Uthmani text or highlighted Mushaf pages.
    3. *Customize & Share Anywhere* — Tailor fonts, translations, and video filters, then export 1080p MP4s ready for Reels, Shorts & TikTok.
  - Bottom pagination dots (Gold `#D4AF37` active pill/dot, dark green `#16382C` inactive) and a large Emerald Green **`Next`** / **`Get Started`** button.
- **3. Main "Quran Reels" Screen (Scrollable `ListView` of 8 Rounded Cards)**:
  - **Header**: `"Quran Reels"` in bold Gold on the left; `"EN"` pill button with Globe icon (`🌐 EN`) and Info icon (`ⓘ`) on the right.
  - **Card 1 — Reciter**: Gold Mic icon, label `"Select Reciter"`, dropdown showing `"Mishary Rashid Alafasy"` (plus Saad Al-Ghamdi, AbdulBaset, Husary, Sudais).
  - **Card 2 — Surah**: Gold Number badge icon, label `"Select Surah"`, dropdown showing `"1 الفاتحة (Al-Fatiha)"` (plus all 114 Surahs via Quran.com API v4).
  - **Card 3 — Ayah Range**: Header row with `"Ayah Range"` on the left and `"7 Ayah"` in Gold on the right. Two inner rows—`"Start Ayah"` and `"Number of Ayahs"`—each with a custom `-` / `+` stepper (Gold text on dark `#050B08` pill background).
  - **Card 4 — Translation**: Gold Translate icon, label `"Translation Language"`, dropdown showing `"English"` (plus Arabic Only, French, Urdu, Indonesian, and optional TTS voiceover).
  - **Card 5 — Mushaf Page**: Gold Book icon, label `"Mushaf page"`, subtitle `"Show the Quran page, recited ayah in red"`, and a toggle switch (Gold `#D4AF37` when active).
  - **Card 6 — Background**: Gold Video icon, label `"Select Background"`, right value `"Tap to choose a b..."` (opens the 3-column Background Selection Modal with `"Your Videos"` and `"Browse"` tabs + search bar).
  - **Card 7 — Text Style**: Gold Font (`Aa`) icon, label `"Text Style"`, right value `"Amiri / Roboto"` (opens the Text Styling BottomSheet with Font family, Arabic Font Size slider, Text/Background Color + Opacity, Position Top/Center/Bottom, and Translation toggle).
  - **Card 8 — Video Filters**: Gold Filter/Sparkle icon, label `"Video Filters"`, right value `"Off"` (opens Video Filter selector: `Off`, `Warm Gold`, `Dark Vignette`, `Soft Blur`, `Cinematic Sepia`, `Emerald Sanctuary`).
  - **Bottom CTA Button**: Large full-width Emerald Green **`▶ Generate`** button.
  - **Footer Note**: Info icon (`ⓘ`) + `"Your reel will be in 9:16 format, perfect for social media."`

---

## 1. Overview & Core Concept

- **What It Does**: Delivers the complete **"Quran Reels"** experience inside a responsive 480px web-framed mobile container—starting with the 3-step Onboarding carousel, flowing into the 8-card scrollable creation studio, opening interactive BottomSheets for Background Selection, Text Styling, and Video Filters, and generating a live 9:16 preview and 1080×1920 @ 30fps MP4 via **Dart Isolates** and **`ffmpeg_kit_flutter`**.
- **Target Audience / Persona**: Creators and developers testing the simulated responsive Web UI first (Phase 1) and exporting high-definition 9:16 Quran Reels on Android (Phase 2).
- **Key Value**: Matches the exact card hierarchy, iconography, color tokens (`#050B08`, `#0E241D`, `#D4AF37`), and interaction flow of the reference design while preserving 100% authentic Quran.com API v4 script and granular Ayah-by-Ayah MP3 caching.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding Carousel (3 Screens)**:
   - Users can swipe through the 3 onboarding slides, tap **`Next`** (which transforms into **`Get Started`** on slide 3), or tap **`Skip`** in the top-right corner to jump straight into the Main Studio.
   - Tapping the Info (`ⓘ`) icon in the Main Header allows re-viewing the Onboarding tutorial or inspecting the **Complete Flutter Source Code & APK Build README**.
2. **Main "Create Reel" Screen (Scrollable `ListView` of 8 Cards)**:
   - Every card uses `#0E241D` surface color, `12px` rounded corners, subtle border framing, and Gold (`#D4AF37`) iconography.
   - Adjusting **Select Surah** dynamically updates the Gold `"X Ayah"` counter in Card 3 and clamps the **Start Ayah** and **Number of Ayahs** `+`/`-` steppers.
   - Toggling **Mushaf page** (`Card 5`) switches the 9:16 reel renderer between standard centered verse calligraphy and a traditional Mushaf page layout where the currently recited Ayah is highlighted in red (`#E53935`).
3. **Interactive Modals & BottomSheets (Cards 6, 7, 8)**:
   - **Card 6 (`Select Background`)**: Opens the Background Selection Modal with **`Your Videos`** (gallery import) and **`Browse`** (starter pack), search bar, and 3-column 9:16 video grid with play icons. Once chosen, Card 6 updates from `"Tap to choose a b..."` to the selected background's name and miniature thumbnail.
   - **Card 7 (`Text Style`)**: Opens the Text Styling BottomSheet (`Amiri / Roboto` font pairing, Arabic Font Size slider, Text & Background Color + Opacity, Position Top/Center/Bottom, and English Translation switch).
   - **Card 8 (`Video Filters`)**: Opens the Video Filter BottomSheet (`Off`, `Warm Gold`, `Dark Vignette`, `Soft Blur`, `Cinematic Sepia`, `Emerald Sanctuary`).
4. **Live 9:16 Preview & `ffmpeg_kit_flutter` Generation (`▶ Generate`)**:
   - Tapping the large Emerald Green **`▶ Generate`** button fetches the authentic `text_uthmani` and translation from `api.quran.com/api/v4`, downloads/loads the cached EveryAyah `SSSAAA.mp3` files for `[startAyah .. startAyah + numberOfAyahs - 1]`, and opens the **9:16 Live Preview & Export Modal** where users can watch sequential playback and save the 1080×1920 @ 30fps MP4 to their gallery.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Centered 480px Responsive Web Simulation Frame**
  - *Chosen Approach*: Wrap the app in a responsive web frame (`ConstrainedBox(constraints: BoxConstraints(maxWidth: 480))`) centered over the `#050B08` backdrop so it looks and behaves identically to a mobile web app on desktop/tablet screens while filling handheld screens edge-to-edge.
  - *Why*: Directly satisfies Requirement 1 ("first simulating a responsive website UI for easy testing" with max width 480px).
- **Decision 2: "Mushaf Page" Highlight Mode + Standard Overlay Mode**
  - *Chosen Approach*: Support Card 5's `"Mushaf page — Show the Quran page, recited ayah in red"` toggle by rendering the surrounding Surah page context with the active recited Ayah highlighted in crimson/red (`#EF5350`) when enabled, or rendering the single active Ayah overlay when disabled.
  - *Why*: Brings Card 5's toggle switch to life as a genuine functional feature in both the live preview and exported video.
- **Decision 3: Authentic Quran.com API v4 + Granular Ayah MP3 Caching + Dart Isolates**
  - *Chosen Approach*: Keep all authentic `text_uthmani` fetches via `api.quran.com/api/v4`, granular `SSSAAA.mp3` caching in Hive/file storage, and background **Dart Isolate + `ffmpeg_kit_flutter`** audio stitching and 1080p MP4 rendering.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│        RESPONSIVE WEB SIMULATION CONTAINER (Max Width: 480px, #050B08)       │
│  ┌────────────────────────────────────┐  ┌────────────────────────────────┐  │
│  │  Onboarding Flow (3 Screens)       │  │  Main "Quran Reels" ListView   │  │
│  │  • Top-Right "Skip"                │  │  • Header: "Quran Reels" | EN ⓘ│  │
│  │  • Centered Vector Illustration    │  │  • Card 1: Select Reciter      │  │
│  │  • White Title & Gray Subtitle     │─►│  • Card 2: Select Surah        │  │
│  │  • Gold / Dark Green Dots          │  │  • Card 3: Ayah Range (+/-)    │  │
│  │  • Emerald [Next / Get Started]    │  │  • Card 4: Translation (EN)    │  │
│  └────────────────────────────────────┘  │  • Card 5: Mushaf Page Switch  │  │
│                                          │  • Card 6: Select Background   │  │
│                                          │  • Card 7: Text Style (Amiri)  │  │
│                                          │  • Card 8: Video Filters (Off) │  │
│                                          │  • CTA: [ ▶ Generate ] + Footer│  │
│                                          └───────────────┬────────────────┘  │
└──────────────────────────────────────────────────────────┼───────────────────┘
                                                           │
         ┌─────────────────────────────────────────────────┼─────────────────┐
         ▼                                                 ▼                 ▼
┌─────────────────────────┐  ┌──────────────────────────────┐  ┌──────────────────────┐
│ BottomSheets & Modals   │  │ Quran.com API v4 + Hive Cache│  │ Isolate + FFmpegKit  │
│ • 3-Col Bg Modal        │  │ • Authentic text_uthmani     │  │ • Stitches SSSAAA.mp3│
│ • Text Style Sheet      │  │ • Granular SSSAAA.mp3 Cache  │  │ • Renders 1080x1920  │
│ • Video Filters Sheet   │  │ • Resilient Gemini/Native TTS│  │   @ 30fps MP4        │
└─────────────────────────┘  └──────────────────────────────┘  └──────────────────────┘
```

### Interactive Component & State Mapping

- **Onboarding State**: `onboardingPage (0..2)` and `hasCompletedOnboarding: bool`. Tapping `Next` increments the page; tapping `Get Started` or `Skip` transitions to the Main `"Quran Reels"` screen.
- **Language Toggle (`EN` / `AR` Globe Button)**: Toggles UI labels between English and Arabic localization strings while keeping the layout crisp.
- **8-Card Studio State (`QuranReelsConfig`)**:
  - `Card 1 (Reciter)`: Updates `selectedReciter` (*Mishary Rashid Alafasy*, etc.).
  - `Card 2 (Surah)`: Updates `selectedSurah` (`1 الفاتحة (Al-Fatiha)`, etc.), dynamically updating Card 3's Gold `"X Ayah"` badge and resetting `startAyah = 1`.
  - `Card 3 (Ayah Range)`: `+` and `-` buttons mutate `startAyah` and `numberOfAyahs` within valid Surah bounds.
  - `Card 4 (Translation Language)`: Selects `"English"`, `"French"`, `"Urdu"`, `"Indonesian"`, or `"None"` (plus optional TTS voiceover narration).
  - `Card 5 (Mushaf Page)`: Toggles `showMushafPage` (highlighting the recited Ayah in red inside the Mushaf frame).
  - `Card 6 (Select Background)`: Opens the 3-column Background Selection Modal (`Your Videos` | `Browse` + search bar) and updates the subtitle from `"Tap to choose a b..."` to the chosen video scene.
  - `Card 7 (Text Style)`: Opens the Text Styling BottomSheet and updates the summary label (e.g., `"Amiri / Roboto • Center"`).
  - `Card 8 (Video Filters)`: Opens the Video Filter picker and updates the summary label (`"Off"`, `"Warm Gold"`, `"Dark Vignette"`, etc.).
- **Bottom `▶ Generate` Button**: Fetches authentic `text_uthmani` from `api.quran.com/api/v4`, downloads/verifies cached `SSSAAA.mp3` files per Ayah, and opens the **9:16 Live Preview & `ffmpeg_kit_flutter` Export Screen** (alongside a built-in **Flutter Source Code & APK README** inspector).
