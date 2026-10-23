import 'dart:convert';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:http/http.dart' as http;

/// ============================================================================
/// AYAH STUDIO — MIDNIGHT AURORA & GLASSMORPHISM (FLUTTER DART)
/// ============================================================================
/// 1. Midnight Aurora Background:
///    - Deep dark blue/black gradient (#0B0F19 to #1A1B2F)
///    - Blurred glowing orbs using Positioned & Container(shape: BoxShape.circle)
///      with high blurRadius (Purple #7B2CBF, Teal #00F5D4, Magenta #FF006E)
/// 2. Glassmorphism Cards:
///    - BackdropFilter with ImageFilter.blur(sigmaX: 10, sigmaY: 10)
///    - Card fill: Colors.white.withOpacity(0.05)
///    - Card border: 1px solid Colors.white.withOpacity(0.10) + subtle white glow
/// 3. Vibrant Glowing Buttons & Accents:
///    - LinearGradient(colors: [Color(0xFF7B2CBF), Color(0xFF00F5D4)])
/// 4. Typography & Authentic RTL Quranic Rendering:
///    - Pure White (#FFFFFF) headings & Soft Gray (#A0AAB2) subtitles
///    - 'Amiri Quran' with TextDirection.rtl and ornate ﴿١﴾ brackets
/// ============================================================================

const Color kMidnightStart = Color(0xFF0B0F19);
const Color kMidnightEnd = Color(0xFF1A1B2F);
const Color kAuroraPurple = Color(0xFF7B2CBF);
const Color kAuroraTeal = Color(0xFF00F5D4);
const Color kAuroraMagenta = Color(0xFFFF006E);
const Color kTextWhite = Color(0xFFFFFFFF);
const Color kTextSoftGray = Color(0xFFA0AAB2);

String toArabicIndicDigits(int number) {
  const arabicDigits = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];
  return number
      .toString()
      .split('')
      .map((ch) {
        final code = ch.codeUnitAt(0) - 48;
        return (code >= 0 && code <= 9) ? arabicDigits[code] : ch;
      })
      .join();
}

String formatAyahWithOrnateNumber(String textUthmani, int ayahNumber) {
  final cleaned = textUthmani
      .replaceAll(RegExp(r'[\uFD3E\uFD3F\u06DD0-9٠-٩]+\s*$'), '')
      .trim();
  final arabicNum = toArabicIndicDigits(ayahNumber);
  return '\u200F$cleaned\u00A0\uFD3F$arabicNum\uFD3E';
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Hive.initFlutter();
  await Hive.openBox<String>('ayah_audio_cache_v1');
  runApp(const AyahStudioApp());
}

class AyahStudioApp extends StatelessWidget {
  const AyahStudioApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Ayah Studio',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: kMidnightStart,
        colorScheme: const ColorScheme.dark(
          primary: kAuroraTeal,
          secondary: kAuroraPurple,
          tertiary: kAuroraMagenta,
          surface: kMidnightEnd,
          onSurface: kTextWhite,
        ),
      ),
      home: const MidnightAuroraStudioScreen(),
    );
  }
}

class GlassmorphicCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final double borderRadius;

  const GlassmorphicCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.borderRadius = 16,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(borderRadius),
        boxShadow: [
          BoxShadow(
            color: Colors.white.withOpacity(0.04),
            blurRadius: 16,
            spreadRadius: 1,
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ui.ImageFilter.blur(sigmaX: 10, sigmaY: 10),
          child: Container(
            padding: padding,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.05),
              borderRadius: BorderRadius.circular(borderRadius),
              border: Border.all(
                color: Colors.white.withOpacity(0.10),
                width: 1.0,
              ),
            ),
            child: child,
          ),
        ),
      ),
    );
  }
}

class AuroraGradientButton extends StatelessWidget {
  final String label;
  final IconData? icon;
  final VoidCallback onTap;

  const AuroraGradientButton({
    super.key,
    required this.label,
    required this.onTap,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 54,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(14),
          gradient: const LinearGradient(
            colors: [kAuroraPurple, kAuroraTeal],
          ),
          boxShadow: [
            BoxShadow(
              color: kAuroraPurple.withOpacity(0.45),
              blurRadius: 18,
              offset: const Offset(-4, 4),
            ),
            BoxShadow(
              color: kAuroraTeal.withOpacity(0.40),
              blurRadius: 18,
              offset: const Offset(4, 4),
            ),
          ],
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (icon != null) ...[
              Icon(icon, color: kTextWhite, size: 20),
              const SizedBox(width: 8),
            ],
            Text(
              label,
              style: const TextStyle(
                color: kTextWhite,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class MidnightAuroraStudioScreen extends StatefulWidget {
  const MidnightAuroraStudioScreen({super.key});

  @override
  State<MidnightAuroraStudioScreen> createState() =>
      _MidnightAuroraStudioScreenState();
}

class _MidnightAuroraStudioScreenState
    extends State<MidnightAuroraStudioScreen> {
  final int _surahId = 1;
  final int _startAyah = 1;
  final int _numberOfAyahs = 3;
  bool _showTranslation = true;
  bool _enableTts = false;
  String _uthmaniVerse = 'بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ';
  String _translation =
      'In the Name of Allah—the Most Compassionate, Most Merciful.';

  @override
  void initState() {
    super.initState();
    _fetchFirstVerse();
  }

  Future<void> _fetchFirstVerse() async {
    try {
      final uri = Uri.parse(
        'https://api.quran.com/api/v4/verses/by_key/$_surahId:$_startAyah'
        '?language=en&words=false&translations=131&fields=text_uthmani',
      );
      final res = await http.get(uri);
      if (res.statusCode == 200) {
        final map = jsonDecode(res.body)['verse'] as Map<String, dynamic>;
        setState(() {
          _uthmaniVerse = map['text_uthmani'] as String? ?? _uthmaniVerse;
        });
      }
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        fit: StackFit.expand,
        children: [
          // 1. Midnight Aurora Gradient (#0B0F19 to #1A1B2F)
          Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [kMidnightStart, kMidnightEnd],
              ),
            ),
          ),

          // 2. Blurred Glowing Aurora Orbs (Purple #7B2CBF, Teal #00F5D4, Magenta #FF006E)
          Positioned(
            top: -60,
            left: -50,
            child: Container(
              width: 240,
              height: 240,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.transparent,
                boxShadow: [
                  BoxShadow(
                    color: kAuroraPurple.withOpacity(0.42),
                    blurRadius: 120,
                    spreadRadius: 40,
                  ),
                ],
              ),
            ),
          ),
          Positioned(
            top: 220,
            right: -70,
            child: Container(
              width: 230,
              height: 230,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.transparent,
                boxShadow: [
                  BoxShadow(
                    color: kAuroraTeal.withOpacity(0.32),
                    blurRadius: 130,
                    spreadRadius: 35,
                  ),
                ],
              ),
            ),
          ),
          Positioned(
            bottom: -60,
            left: 20,
            child: Container(
              width: 220,
              height: 220,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.transparent,
                boxShadow: [
                  BoxShadow(
                    color: kAuroraMagenta.withOpacity(0.28),
                    blurRadius: 130,
                    spreadRadius: 35,
                  ),
                ],
              ),
            ),
          ),

          // 3. Foreground Glassmorphism Content
          Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 480),
              child: SafeArea(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      // Header
                      Row(
                        children: [
                          Container(
                            width: 38,
                            height: 38,
                            alignment: Alignment.center,
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(11),
                              gradient: const LinearGradient(
                                colors: [kAuroraPurple, kAuroraTeal],
                              ),
                            ),
                            child: const Text(
                              'ق',
                              style: TextStyle(
                                color: kTextWhite,
                                fontSize: 19,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          const SizedBox(width: 12),
                          const Text(
                            'Ayah Studio',
                            style: TextStyle(
                              color: kTextWhite,
                              fontSize: 21,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),

                      // 9:16 Preview with Glassmorphic Verse Card & RTL Amiri Quran
                      Expanded(
                        child: AspectRatio(
                          aspectRatio: 9 / 16,
                          child: GlassmorphicCard(
                            child: Center(
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Directionality(
                                    textDirection: TextDirection.rtl,
                                    child: Text(
                                      formatAyahWithOrnateNumber(
                                        _uthmaniVerse,
                                        _startAyah,
                                      ),
                                      textDirection: TextDirection.rtl,
                                      textAlign: TextAlign.center,
                                      style: const TextStyle(
                                        fontFamily: 'Amiri Quran',
                                        color: kTextWhite,
                                        fontSize: 30,
                                        height: 1.55,
                                      ),
                                    ),
                                  ),
                                  if (_showTranslation) ...[
                                    const SizedBox(height: 10),
                                    Text(
                                      _translation,
                                      textAlign: TextAlign.center,
                                      style: const TextStyle(
                                        color: kTextSoftGray,
                                        fontSize: 14,
                                      ),
                                    ),
                                  ],
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),

                      // Frosted Glass Toggles Card
                      GlassmorphicCard(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 14,
                          vertical: 8,
                        ),
                        child: Column(
                          children: [
                            SwitchListTile(
                              contentPadding: EdgeInsets.zero,
                              activeColor: kAuroraTeal,
                              title: const Text(
                                'Show Translation',
                                style: TextStyle(
                                  color: kTextWhite,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              subtitle: const Text(
                                'Display English translation under Arabic script',
                                style: TextStyle(
                                  color: kTextSoftGray,
                                  fontSize: 12,
                                ),
                              ),
                              value: _showTranslation,
                              onChanged: (v) =>
                                  setState(() => _showTranslation = v),
                            ),
                            SwitchListTile(
                              contentPadding: EdgeInsets.zero,
                              activeColor: kAuroraTeal,
                              title: const Text(
                                'Enable TTS',
                                style: TextStyle(
                                  color: kTextWhite,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              subtitle: const Text(
                                'Narrate translation with Gemini TTS / Native fallback',
                                style: TextStyle(
                                  color: kTextSoftGray,
                                  fontSize: 12,
                                ),
                              ),
                              value: _enableTts,
                              onChanged: (v) => setState(() => _enableTts = v),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 12),

                      // Vibrant Purple-to-Cyan Glowing Button
                      AuroraGradientButton(
                        label: 'Generate ($_numberOfAyahs Ayahs)',
                        icon: Icons.play_arrow_rounded,
                        onTap: () {},
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
