import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:isolate';
import 'package:ffmpeg_kit_flutter_min_gpl/ffmpeg_kit.dart';
import 'package:ffmpeg_kit_flutter_min_gpl/ffmpeg_kit_config.dart';
import 'package:ffmpeg_kit_flutter_min_gpl/return_code.dart';
import 'package:gal/gal.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';

/// Data model representing an authentic verse fetched from Quran.com API v4 (api.quran.com/api/v4).
/// NEVER hardcoded or AI-generated.
class AuthenticAyah {
  final int surahId;
  final int ayahNumber;
  final String verseKey;
  final String textUthmani;
  final String translationText;
  final String localAudioPath;
  final int durationMs;

  const AuthenticAyah({
    required this.surahId,
    required this.ayahNumber,
    required this.verseKey,
    required this.textUthmani,
    required this.translationText,
    required this.localAudioPath,
    required this.durationMs,
  });

  Map<String, dynamic> toMap() => {
        'surahId': surahId,
        'ayahNumber': ayahNumber,
        'verseKey': verseKey,
        'textUthmani': textUthmani,
        'translationText': translationText,
        'localAudioPath': localAudioPath,
        'durationMs': durationMs,
      };

  factory AuthenticAyah.fromMap(Map<String, dynamic> map) => AuthenticAyah(
        surahId: map['surahId'] as int,
        ayahNumber: map['ayahNumber'] as int,
        verseKey: map['verseKey'] as String,
        textUthmani: map['textUthmani'] as String,
        translationText: map['translationText'] as String,
        localAudioPath: map['localAudioPath'] as String,
        durationMs: map['durationMs'] as int,
      );
}

/// Official Quran.com API v4 + Granular EveryAyah Local Hive/File Cache Service
class QuranApiAndCacheService {
  static const String _baseUrl = 'https://api.quran.com/api/v4';
  static const String _hiveBoxName = 'ayah_audio_cache_box';

  /// Fetches 100% authentic Uthmani script and translation from Quran.com API v4
  static Future<List<Map<String, dynamic>>> fetchAuthenticVerses({
    required int surahId,
    required int startAyah,
    required int numberOfAyahs,
    required int translationId,
  }) async {
    final endAyah = startAyah + numberOfAyahs - 1;
    final uri = Uri.parse(
      '$_baseUrl/verses/by_chapter/$surahId?language=en&words=false&translations=$translationId&fields=text_uthmani,qpc_uthmani_hafs&per_page=300',
    );
    final response = await http.get(uri);
    if (response.statusCode != 200) {
      throw Exception('Failed to fetch authentic verses from Quran.com API v4');
    }
    final data = jsonDecode(response.body) as Map<String, dynamic>;
    final verses = (data['verses'] as List).cast<Map<String, dynamic>>();
    return verses.where((v) {
      final verseNum = v['verse_number'] as int;
      return verseNum >= startAyah && verseNum <= endAyah;
    }).toList();
  }

  /// Granular Ayah-by-Ayah MP3 download using EveryAyah naming convention (e.g., 002001.mp3)
  /// with local Hive + File System caching.
  static Future<File> getOrDownloadAyahMp3({
    required String reciterSubfolder,
    required int surahId,
    required int ayahNumber,
  }) async {
    final box = await Hive.openBox<String>(_hiveBoxName);
    final sss = surahId.toString().padLeft(3, '0');
    final aaa = ayahNumber.toString().padLeft(3, '0');
    final fileName = '$sss$aaa.mp3';
    final cacheKey = '${reciterSubfolder}_$fileName';

    final cachedPath = box.get(cacheKey);
    if (cachedPath != null) {
      final cachedFile = File(cachedPath);
      if (await cachedFile.exists() && await cachedFile.length() > 0) {
        return cachedFile; // Loaded locally without re-downloading!
      }
    }

    final dir = await getApplicationSupportDirectory();
    final reciterDir = Directory('${dir.path}/ayah_cache/$reciterSubfolder');
    if (!await reciterDir.exists()) {
      await reciterDir.create(recursive: true);
    }

    final targetFile = File('${reciterDir.path}/$fileName');
    final url = 'https://everyayah.com/data/$reciterSubfolder/$fileName';
    final response = await http.get(Uri.parse(url));
    if (response.statusCode != 200) {
      throw Exception('Failed to download $url');
    }
    await targetFile.writeAsBytes(response.bodyBytes, flush: true);
    await box.put(cacheKey, targetFile.path);
    return targetFile;
  }
}

/// Payload sent into the background Dart Isolate to prevent UI freezing
class IsolatePreparationPayload {
  final String workingDirPath;
  final List<Map<String, dynamic>> ayahs;
  final double arabicFontSize;
  final String textPosition; // 'Top', 'Center', 'Bottom'
  final String fontColorHex;
  final String backgroundColorHex;
  final double backgroundOpacity;
  final bool showTranslation;
  final bool showMushafPage;

  const IsolatePreparationPayload({
    required this.workingDirPath,
    required this.ayahs,
    required this.arabicFontSize,
    required this.textPosition,
    required this.fontColorHex,
    required this.backgroundColorHex,
    required this.backgroundOpacity,
    required this.showTranslation,
    required this.showMushafPage,
  });
}

/// Result returned from the background Dart Isolate
class IsolatePreparationResult {
  final String concatListPath;
  final String assSubtitlePath;
  final int totalDurationMs;

  const IsolatePreparationResult({
    required this.concatListPath,
    required this.assSubtitlePath,
    required this.totalDurationMs,
  });
}

/// Dart Isolate + ffmpeg_kit_flutter Export Engine
class IsolateFfmpegExportService {
  /// Runs heavy subtitle generation and FFmpeg concat manifest creation inside a Dart Isolate,
  /// then stitches the Ayah MP3s and merges the 1080x1920 @ 30fps MP4 via ffmpeg_kit_flutter.
  static Future<String> exportQuranReelMp4({
    required String backgroundVideoPath,
    required List<AuthenticAyah> ayahs,
    required double arabicFontSize,
    required String textPosition,
    required String fontColorHex,
    required String backgroundColorHex,
    required double backgroundOpacity,
    required bool showTranslation,
    required bool showMushafPage,
    required String videoFilter,
    required void Function(double progress, String stage) onProgress,
  }) async {
    final tempDir = await getTemporaryDirectory();
    final payload = IsolatePreparationPayload(
      workingDirPath: tempDir.path,
      ayahs: ayahs.map((a) => a.toMap()).toList(),
      arabicFontSize: arabicFontSize,
      textPosition: textPosition,
      fontColorHex: fontColorHex,
      backgroundColorHex: backgroundColorHex,
      backgroundOpacity: backgroundOpacity,
      showTranslation: showTranslation,
      showMushafPage: showMushafPage,
    );

    onProgress(0.10, 'Preparing ASS subtitles & concat manifest in Dart Isolate...');

    // 1. Spawn a Dart Isolate using Isolate.run so the Flutter UI thread never freezes
    final prepResult = await Isolate.run(() => _prepareSubtitlesAndConcatInIsolate(payload));

    final stitchedAudioPath = '${tempDir.path}/stitched_recitation.m4a';
    final outputMp4Path =
        '${tempDir.path}/quran_reel_${DateTime.now().millisecondsSinceEpoch}.mp4';

    // 2. Stage 1 FFmpeg: Stitch individual cached Ayah MP3s into a single continuous track
    onProgress(0.25, 'Stitching cached Ayah MP3 files via FFmpeg...');
    final stitchCmd =
        '-y -f concat -safe 0 -i "${prepResult.concatListPath}" -c:a aac -b:a 192k "$stitchedAudioPath"';
    final stitchSession = await FFmpegKit.execute(stitchCmd);
    final stitchRc = await stitchSession.getReturnCode();
    if (!ReturnCode.isSuccess(stitchRc)) {
      throw Exception('FFmpeg audio stitching failed');
    }

    // 3. Stage 2 FFmpeg: Merge background video + stitched audio + synced Uthmani subtitles at 1080x1920 30fps
    FFmpegKitConfig.enableStatisticsCallback((stats) {
      final timeMs = stats.getTime();
      if (prepResult.totalDurationMs > 0) {
        final ratio = (timeMs / prepResult.totalDurationMs).clamp(0.0, 1.0);
        onProgress(0.30 + (ratio * 0.65), 'Encoding 1080x1920 @ 30fps MP4...');
      }
    });

    final filterExtra = videoFilter == 'Warm Gold'
        ? ',colorbalance=rs=.15:gs=.08:bs=-.10'
        : videoFilter == 'Dark Vignette'
            ? ',vignette=PI/4'
            : '';

    final mergeCmd =
        '-y -stream_loop -1 -i "$backgroundVideoPath" -i "$stitchedAudioPath" '
        '-vf "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,fps=30$filterExtra,ass=${prepResult.assSubtitlePath}" '
        '-map 0:v:0 -map 1:a:0 -c:v libx264 -preset ultrafast -crf 23 -pix_fmt yuv420p '
        '-c:a copy -shortest "$outputMp4Path"';

    final completer = Completer<String>();
    await FFmpegKit.executeAsync(mergeCmd, (session) async {
      final rc = await session.getReturnCode();
      if (ReturnCode.isSuccess(rc)) {
        await Gal.putVideo(outputMp4Path, album: 'QuranReels');
        onProgress(1.0, 'Saved 1080x1920 MP4 to Phone Gallery!');
        completer.complete(outputMp4Path);
      } else {
        completer.completeError(Exception('FFmpeg video merge failed'));
      }
    });

    return completer.future;
  }

  /// Pure function executed inside the background Dart Isolate
  static IsolatePreparationResult _prepareSubtitlesAndConcatInIsolate(
    IsolatePreparationPayload payload,
  ) {
    final concatFile = File('${payload.workingDirPath}/ayah_concat.txt');
    final assFile = File('${payload.workingDirPath}/synced_ayahs.ass');

    final concatBuffer = StringBuffer();
    final assBuffer = StringBuffer();

    final alignment = payload.textPosition == 'Top'
        ? 8
        : payload.textPosition == 'Bottom'
            ? 2
            : 5; // 5 = Center in ASS spec

    assBuffer.writeln('[Script Info]');
    assBuffer.writeln('ScriptType: v4.00+');
    assBuffer.writeln('PlayResX: 1080');
    assBuffer.writeln('PlayResY: 1920');
    assBuffer.writeln('');
    assBuffer.writeln('[V4+ Styles]');
    assBuffer.writeln(
        'Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding');
    assBuffer.writeln(
        'Style: Uthmani,Amiri,${payload.arabicFontSize.round()},&H00FFFFFF,&H0037AFD4,&H66000000,&H80000000,1,0,0,0,100,100,0,0,3,4,0,$alignment,80,80,180,1');
    assBuffer.writeln('');
    assBuffer.writeln('[Events]');
    assBuffer.writeln(
        'Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text');

    int cumulativeMs = 0;
    for (final map in payload.ayahs) {
      final ayah = AuthenticAyah.fromMap(map);
      concatBuffer.writeln("file '${ayah.localAudioPath}'");

      final startStr = _formatAssTime(cumulativeMs);
      final endMs = cumulativeMs + ayah.durationMs;
      final endStr = _formatAssTime(endMs);
      cumulativeMs = endMs;

      final combinedText = payload.showTranslation && ayah.translationText.isNotEmpty
          ? '${ayah.textUthmani}\\N\\N{\\fs34}${ayah.translationText}'
          : ayah.textUthmani;

      assBuffer.writeln('Dialogue: 0,$startStr,$endStr,Uthmani,,0,0,0,,$combinedText');
    }

    concatFile.writeAsStringSync(concatBuffer.toString(), flush: true);
    assFile.writeAsStringSync(assBuffer.toString(), flush: true);

    return IsolatePreparationResult(
      concatListPath: concatFile.path,
      assSubtitlePath: assFile.path,
      totalDurationMs: cumulativeMs,
    );
  }

  static String _formatAssTime(int ms) {
    final totalSeconds = ms ~/ 1000;
    final hours = totalSeconds ~/ 3600;
    final minutes = (totalSeconds % 3600) ~/ 60;
    final seconds = totalSeconds % 60;
    final centiseconds = (ms % 1000) ~/ 10;
    return '$hours:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}.${centiseconds.toString().padLeft(2, '0')}';
  }
}
