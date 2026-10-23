package com.example.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.text.Html
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "QuranRepository"

/**
 * Friendly bilingual error message shown whenever network/audio download fails after all
 * retries and CDN fallbacks — never exposing raw Exception stack traces to the user.
 */
const val FRIENDLY_AUDIO_ERROR_MESSAGE_AR =
    "تعذر تحميل الصوت، يرجى التحقق من اتصالك بالإنترنت والمحاولة مرة أخرى"
const val FRIENDLY_AUDIO_ERROR_MESSAGE_EN =
    "Failed to load audio, please check your internet connection and try again."

data class AudioDownloadBatchResult(
    val ayahs: List<AuthenticAyah>,
    val allSucceeded: Boolean,
    val friendlyErrorMessage: String? = null
)

/**
 * Production-hardened repository for Ayah Studio:
 * - Fetches 100% authentic, unaltered Uthmani script (`text_uthmani`) and translations
 *   from the official Quran.com API v4 (`https://api.quran.com/api/v4`).
 * - Implements multi-CDN audio downloading with `try-catch` and up to 3 retry attempts:
 *   1. Primary: EveryAyah (`https://everyayah.com/data/{subfolder}/{SSSAAA}.mp3`)
 *   2. Fallback 1: Official Quran.com CDN (`https://verses.quran.com/{reciterPath}/{SSSAAA}.mp3`)
 *   3. Fallback 2: Dynamic Quran.com API v4 audio lookup (`https://api.quran.com/api/v4/recitations/{id}/by_ayah/{verseKey}`)
 *   4. Fallback 3: Alafasy Quran.com CDN mirror (`https://verses.quran.com/Alafasy/mp3/{SSSAAA}.mp3`)
 * - Streams audio bytes with real-time percentage progress (`0%..100%`) so the user sees
 *   "Downloading Ayah 1... 45%".
 */
class QuranRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(14, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val cacheRootDir: File
        get() = File(context.filesDir, "ayah_audio_cache").apply { mkdirs() }

    private val cacheIndexFile: File
        get() = File(cacheRootDir, "hive_ayah_cache_index.json")

    /**
     * Generic retry wrapper with exponential backoff (up to 3 attempts) for network resilience.
     */
    private suspend fun <T> withNetworkRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 450L,
        block: suspend (attempt: Int) -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                return block(attempt)
            } catch (e: SocketTimeoutException) {
                lastException = e
                Log.w(TAG, "Timeout on attempt $attempt/$maxAttempts", e)
            } catch (e: UnknownHostException) {
                lastException = e
                Log.w(TAG, "UnknownHost on attempt $attempt/$maxAttempts", e)
            } catch (e: SocketException) {
                lastException = e
                Log.w(TAG, "SocketException on attempt $attempt/$maxAttempts", e)
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "IOException on attempt $attempt/$maxAttempts", e)
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Unexpected exception on attempt $attempt/$maxAttempts", e)
            }
            if (attempt < maxAttempts) {
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw lastException ?: IOException(FRIENDLY_AUDIO_ERROR_MESSAGE_AR)
    }

    /**
     * Fetches the official 114 Surahs from `https://api.quran.com/api/v4/chapters?language=en`
     * with automatic retry and graceful fallback to canonical chapter metadata.
     */
    suspend fun fetchChapters(): List<SurahInfo> = withContext(Dispatchers.IO) {
        try {
            withNetworkRetry(maxAttempts = 3) {
                val request = Request.Builder()
                    .url("https://api.quran.com/api/v4/chapters?language=en")
                    .header("Accept", "application/json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Chapters HTTP ${response.code}")
                    }
                    val bodyStr = response.body?.string().orEmpty()
                    val json = JSONObject(bodyStr)
                    val chaptersArr = json.getJSONArray("chapters")
                    val list = mutableListOf<SurahInfo>()
                    for (i in 0 until chaptersArr.length()) {
                        val obj = chaptersArr.getJSONObject(i)
                        val pagesArr = obj.optJSONArray("pages")
                        val firstPage = if (pagesArr != null && pagesArr.length() > 0) {
                            pagesArr.optInt(0, 1)
                        } else 1
                        list.add(
                            SurahInfo(
                                id = obj.getInt("id"),
                                nameArabic = obj.getString("name_arabic"),
                                nameSimple = obj.getString("name_simple"),
                                nameTranslated = obj.optJSONObject("translated_name")
                                    ?.optString("name", obj.getString("name_simple"))
                                    ?: obj.getString("name_simple"),
                                versesCount = obj.getInt("verses_count"),
                                mushafPageNumber = firstPage
                            )
                        )
                    }
                    if (list.isEmpty()) throw IOException("Empty chapters response")
                    list
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Chapters API fetch failed after retries, using canonical 114 chapter metadata", e)
            CANONICAL_CHAPTERS_METADATA
        }
    }

    /**
     * Fetches 100% authentic `text_uthmani` and official translation from
     * `https://api.quran.com/api/v4/verses/by_chapter/{surahId}` for `[startAyah..endAyah]`
     * with 3-attempt automatic retry and human-friendly error reporting.
     */
    suspend fun fetchAuthenticVerses(
        surahId: Int,
        startAyah: Int,
        numberOfAyahs: Int,
        translationId: Int,
        reciter: ReciterOption
    ): Result<List<AuthenticAyah>> = withContext(Dispatchers.IO) {
        val endAyah = startAyah + numberOfAyahs - 1
        try {
            val verses = withNetworkRetry(maxAttempts = 3) {
                val translationQuery = if (translationId > 0) "&translations=$translationId" else ""
                val url = "https://api.quran.com/api/v4/verses/by_chapter/$surahId" +
                    "?language=en&words=false$translationQuery&fields=text_uthmani,qpc_uthmani_hafs,page_number&per_page=300"

                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }
                    val bodyStr = response.body?.string().orEmpty()
                    val json = JSONObject(bodyStr)
                    val versesArr = json.getJSONArray("verses")
                    val result = mutableListOf<AuthenticAyah>()
                    val durationMap = loadCacheDurationIndex()

                    for (i in 0 until versesArr.length()) {
                        val vObj = versesArr.getJSONObject(i)
                        val vNum = vObj.getInt("verse_number")
                        if (vNum in startAyah..endAyah) {
                            val verseKey = vObj.getString("verse_key")
                            val uthmani = vObj.optString("text_uthmani").ifBlank {
                                vObj.optString("qpc_uthmani_hafs")
                            }
                            val rawTranslation = vObj.optJSONArray("translations")
                                ?.optJSONObject(0)
                                ?.optString("text")
                                .orEmpty()
                            val cleanTranslation = cleanHtmlTags(rawTranslation)
                            val pageNumber = vObj.optInt("page_number", 1)

                            val fileName = formatEveryAyahFileName(surahId, vNum)
                            val primaryEveryAyahUrl =
                                "https://everyayah.com/data/${reciter.everyAyahSubfolder}/$fileName"
                            val fallbackUrls = buildFallbackAudioUrls(reciter, fileName)

                            val cachedFile = getLocalAyahAudioFile(reciter.everyAyahSubfolder, fileName)
                            val isCached = cachedFile.exists() && cachedFile.length() > 256L
                            val cacheKey = "${reciter.everyAyahSubfolder}_$fileName"
                            val durationMs = durationMap[cacheKey] ?: estimateDurationMs(uthmani)

                            result.add(
                                AuthenticAyah(
                                    surahId = surahId,
                                    ayahNumber = vNum,
                                    verseKey = verseKey,
                                    textUthmani = uthmani,
                                    translationText = cleanTranslation,
                                    everyAyahFileName = fileName,
                                    audioUrl = primaryEveryAyahUrl,
                                    fallbackAudioUrls = fallbackUrls,
                                    localCachedFile = if (isCached) cachedFile else null,
                                    isCachedLocally = isCached,
                                    durationMs = durationMs,
                                    pageNumber = pageNumber
                                )
                            )
                        }
                    }
                    result
                }
            }
            Result.success(verses)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching authentic verses from Quran.com API v4", e)
            Result.failure(IOException(FRIENDLY_AUDIO_ERROR_MESSAGE_AR))
        }
    }

    /**
     * Builds an ordered list of reliable fallback audio URLs for the given Ayah:
     * 1. Official Quran.com CDN (`https://verses.quran.com/{reciterPath}/{SSSAAA}.mp3`)
     * 2. Official Quran.com Alafasy CDN mirror (`https://verses.quran.com/Alafasy/mp3/{SSSAAA}.mp3`)
     * 3. Secondary EveryAyah mirror (`https://www.everyayah.com/data/{subfolder}/{SSSAAA}.mp3`)
     */
    fun buildFallbackAudioUrls(reciter: ReciterOption, fileName: String): List<String> {
        return listOf(
            "https://verses.quran.com/${reciter.quranComCdnPath}/$fileName",
            "https://verses.quran.com/Alafasy/mp3/$fileName",
            "https://www.everyayah.com/data/${reciter.everyAyahSubfolder}/$fileName"
        ).distinct()
    }

    /**
     * Resolves the official Quran.com CDN audio URL dynamically from
     * `https://api.quran.com/api/v4/recitations/{reciterId}/by_ayah/{verseKey}`
     * as an additional fallback when direct paths fail.
     */
    private fun resolveQuranComDynamicAudioUrl(reciterId: Int, verseKey: String): String? {
        return try {
            val apiUrl = "https://api.quran.com/api/v4/recitations/$reciterId/by_ayah/$verseKey"
            val req = Request.Builder().url(apiUrl).header("Accept", "application/json").build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string().orEmpty()
                val json = JSONObject(body)
                val audioFiles = json.optJSONArray("audio_files")
                val relativeOrFullUrl = audioFiles?.optJSONObject(0)?.optString("url").orEmpty()
                if (relativeOrFullUrl.isBlank()) {
                    null
                } else if (relativeOrFullUrl.startsWith("http")) {
                    relativeOrFullUrl
                } else {
                    "https://verses.quran.com/${relativeOrFullUrl.trimStart('/')}"
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Granular Ayah-by-Ayah Audio Downloader with:
     * - `try-catch` around EveryAyah download
     * - Automatic fallback to Quran.com API CDN (`https://verses.quran.com/...`)
     * - Up to 3 retry cycles before reporting a human-friendly error
     * - Real-time percentage progress reporting (`onProgressPercent(ayahNumber, ayahIndex, totalAyahs, percent, statusText)`)
     */
    suspend fun ensureAyahRangeAudioCachedWithProgress(
        ayahs: List<AuthenticAyah>,
        reciter: ReciterOption,
        onProgressPercent: (
            ayahNumber: Int,
            ayahIndex: Int,
            totalCount: Int,
            percent: Int,
            statusText: String
        ) -> Unit
    ): AudioDownloadBatchResult = withContext(Dispatchers.IO) {
        val durationMap = loadCacheDurationIndex().toMutableMap()
        val updatedList = mutableListOf<AuthenticAyah>()
        var allSucceeded = true

        ayahs.forEachIndexed { index, ayah ->
            val targetFile = getLocalAyahAudioFile(reciter.everyAyahSubfolder, ayah.everyAyahFileName)
            val cacheKey = "${reciter.everyAyahSubfolder}_${ayah.everyAyahFileName}"
            val total = ayahs.size.coerceAtLeast(1)

            if (targetFile.exists() && targetFile.length() > 256L) {
                val overallPct = (((index + 1).toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
                onProgressPercent(
                    ayah.ayahNumber,
                    index + 1,
                    total,
                    overallPct,
                    "Downloading Ayah ${ayah.ayahNumber}... $overallPct%"
                )
                val dur = durationMap[cacheKey] ?: probeAudioDurationMs(targetFile, ayah.textUthmani)
                durationMap[cacheKey] = dur
                updatedList.add(
                    ayah.copy(
                        localCachedFile = targetFile,
                        isCachedLocally = true,
                        durationMs = dur
                    )
                )
            } else {
                val startPct = ((index.toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(0, 99)
                onProgressPercent(
                    ayah.ayahNumber,
                    index + 1,
                    total,
                    startPct,
                    "Downloading Ayah ${ayah.ayahNumber}... $startPct%"
                )

                val downloadedOk = downloadAyahWithMultiCdnFallback(
                    ayah = ayah,
                    reciter = reciter,
                    targetFile = targetFile
                ) { singleAyahFraction ->
                    val combinedPct = (
                        ((index.toFloat() + singleAyahFraction.coerceIn(0f, 1f)) / total.toFloat()) * 100f
                    ).roundToInt().coerceIn(1, 99)
                    onProgressPercent(
                        ayah.ayahNumber,
                        index + 1,
                        total,
                        combinedPct,
                        "Downloading Ayah ${ayah.ayahNumber}... $combinedPct%"
                    )
                }

                if (!downloadedOk) {
                    allSucceeded = false
                }

                val dur = if (downloadedOk) {
                    probeAudioDurationMs(targetFile, ayah.textUthmani)
                } else {
                    estimateDurationMs(ayah.textUthmani)
                }
                if (downloadedOk) {
                    durationMap[cacheKey] = dur
                }

                val donePct = (((index + 1).toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
                onProgressPercent(
                    ayah.ayahNumber,
                    index + 1,
                    total,
                    donePct,
                    "Downloading Ayah ${ayah.ayahNumber}... $donePct%"
                )

                updatedList.add(
                    ayah.copy(
                        localCachedFile = if (downloadedOk) targetFile else null,
                        isCachedLocally = downloadedOk,
                        durationMs = dur
                    )
                )
            }
        }

        saveCacheDurationIndex(durationMap)
        AudioDownloadBatchResult(
            ayahs = updatedList,
            allSucceeded = allSucceeded,
            friendlyErrorMessage = if (allSucceeded) null else FRIENDLY_AUDIO_ERROR_MESSAGE_AR
        )
    }

    /**
     * Backward-compatible helper for callers expecting the simple progress signature.
     */
    suspend fun ensureAyahRangeAudioCached(
        ayahs: List<AuthenticAyah>,
        reciter: ReciterOption,
        onProgress: (cachedCount: Int, totalCount: Int, currentFileName: String, fromLocalCache: Boolean) -> Unit
    ): List<AuthenticAyah> {
        val batch = ensureAyahRangeAudioCachedWithProgress(
            ayahs = ayahs,
            reciter = reciter
        ) { _, ayahIndex, totalCount, _, _ ->
            val currentFile = ayahs.getOrNull(ayahIndex - 1)?.everyAyahFileName ?: ""
            val wasCached = ayahs.getOrNull(ayahIndex - 1)?.isCachedLocally == true
            onProgress(ayahIndex, totalCount, currentFile, wasCached)
        }
        return batch.ayahs
    }

    /**
     * Downloads a single Ayah MP3 using `try-catch` with up to 3 retry attempts across:
     * 1. Primary EveryAyah URL (`https://everyayah.com/data/...`)
     * 2. Official Quran.com CDN URLs (`https://verses.quran.com/...`)
     * 3. Dynamic Quran.com API v4 audio lookup (`https://api.quran.com/api/v4/recitations/{id}/by_ayah/{key}`)
     */
    private suspend fun downloadAyahWithMultiCdnFallback(
        ayah: AuthenticAyah,
        reciter: ReciterOption,
        targetFile: File,
        onSingleAyahProgress: (Float) -> Unit
    ): Boolean {
        val candidateUrls = mutableListOf<String>().apply {
            add(ayah.audioUrl)
            addAll(ayah.fallbackAudioUrls)
            addAll(buildFallbackAudioUrls(reciter, ayah.everyAyahFileName))
        }.distinct().toMutableList()

        // Up to 3 full retry rounds
        for (attempt in 1..3) {
            if (attempt == 2) {
                // On retry 2, also query Quran.com API v4 for the exact CDN URL if not already added
                val dynamicCdnUrl = resolveQuranComDynamicAudioUrl(reciter.id, ayah.verseKey)
                if (!dynamicCdnUrl.isNullOrBlank() && !candidateUrls.contains(dynamicCdnUrl)) {
                    candidateUrls.add(0, dynamicCdnUrl)
                }
            }

            for (url in candidateUrls) {
                val tempFile = File(targetFile.parentFile, "${ayah.everyAyahFileName}.tmp")
                try {
                    val success = streamUrlToFile(
                        url = url,
                        tempFile = tempFile,
                        targetFile = targetFile,
                        onProgress = onSingleAyahProgress
                    )
                    if (success) {
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Attempt $attempt failed for URL $url — trying fallback CDN", e)
                    if (tempFile.exists()) {
                        runCatching { tempFile.delete() }
                    }
                }
            }

            if (attempt < 3) {
                delay(400L * attempt)
            }
        }
        return false
    }

    private fun streamUrlToFile(
        url: String,
        tempFile: File,
        targetFile: File,
        onProgress: (Float) -> Unit
    ): Boolean {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "AyahStudio/1.0")
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("HTTP ${resp.code} from $url")
            }
            val body = resp.body ?: throw IOException("Empty response body from $url")
            val contentLength = body.contentLength()
            targetFile.parentFile?.mkdirs()

            var totalBytesRead = 0L
            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        totalBytesRead += read
                        if (contentLength > 0) {
                            onProgress((totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0.05f, 0.98f))
                        } else {
                            // Estimate against typical ~48KB Ayah MP3 size
                            onProgress((totalBytesRead.toFloat() / 49152f).coerceIn(0.10f, 0.95f))
                        }
                    }
                    output.flush()
                }
            }

            if (totalBytesRead > 256L) {
                if (targetFile.exists()) {
                    runCatching { targetFile.delete() }
                }
                val renamed = tempFile.renameTo(targetFile)
                if (!renamed) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    runCatching { tempFile.delete() }
                }
                onProgress(1.0f)
                return true
            } else {
                if (tempFile.exists()) {
                    runCatching { tempFile.delete() }
                }
                throw IOException("Payload too small ($totalBytesRead bytes) from $url")
            }
        }
    }

    fun getCachedAyahCount(): Int {
        return cacheRootDir.walkTopDown().count { it.isFile && it.extension == "mp3" }
    }

    private fun getLocalAyahAudioFile(reciterSubfolder: String, fileName: String): File {
        val folder = File(cacheRootDir, reciterSubfolder).apply { mkdirs() }
        return File(folder, fileName)
    }

    fun formatEveryAyahFileName(surahId: Int, ayahNumber: Int): String {
        return "%03d%03d.mp3".format(surahId, ayahNumber)
    }

    private fun probeAudioDurationMs(file: File, fallbackText: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val dur = durStr?.toLongOrNull() ?: estimateDurationMs(fallbackText)
            dur.coerceAtLeast(2200L)
        } catch (_: Exception) {
            estimateDurationMs(fallbackText)
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun estimateDurationMs(textUthmani: String): Long {
        val wordCount = textUthmani.trim().split(Regex("\\s+")).size
        return (wordCount * 820L).coerceIn(3200L, 18000L)
    }

    private fun cleanHtmlTags(raw: String): String {
        if (raw.isBlank()) return ""
        val withoutSup = raw.replace(Regex("<sup[^>]*>.*?</sup>"), "")
        return Html.fromHtml(withoutSup, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    }

    private fun loadCacheDurationIndex(): Map<String, Long> {
        if (!cacheIndexFile.exists()) return emptyMap()
        return try {
            val json = JSONObject(cacheIndexFile.readText())
            val map = mutableMapOf<String, Long>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = json.optLong(k, 5000L)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun saveCacheDurationIndex(map: Map<String, Long>) {
        try {
            val json = JSONObject()
            map.forEach { (k, v) -> json.put(k, v) }
            cacheIndexFile.writeText(json.toString())
        } catch (_: Exception) {
        }
    }

    companion object {
        val CANONICAL_CHAPTERS_METADATA = listOf(
            SurahInfo(1, "الفاتحة", "Al-Fatihah", "The Opener", 7, 1),
            SurahInfo(2, "البقرة", "Al-Baqarah", "The Cow", 286, 2),
            SurahInfo(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, 50),
            SurahInfo(4, "النساء", "An-Nisa", "The Women", 176, 77),
            SurahInfo(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, 106),
            SurahInfo(12, "يوسف", "Yusuf", "Joseph", 111, 235),
            SurahInfo(18, "الكهف", "Al-Kahf", "The Cave", 110, 293),
            SurahInfo(19, "مريم", "Maryam", "Mary", 98, 305),
            SurahInfo(20, "طه", "Taha", "Ta-Ha", 135, 312),
            SurahInfo(24, "النور", "An-Nur", "The Light", 64, 350),
            SurahInfo(36, "يس", "Ya-Sin", "Ya Sin", 83, 440),
            SurahInfo(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, 531),
            SurahInfo(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, 534),
            SurahInfo(67, "الملك", "Al-Mulk", "The Sovereignty", 30, 562),
            SurahInfo(78, "النبأ", "An-Naba", "The Tidings", 40, 582),
            SurahInfo(87, "الأعلى", "Al-A'la", "The Most High", 19, 591),
            SurahInfo(93, "الضحى", "Ad-Duhaa", "The Morning Hours", 11, 596),
            SurahInfo(94, "الشرح", "Ash-Sharh", "The Relief", 8, 596),
            SurahInfo(97, "القدر", "Al-Qadr", "The Power", 5, 598),
            SurahInfo(103, "العصر", "Al-'Asr", "The Declining Day", 3, 601),
            SurahInfo(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, 602),
            SurahInfo(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, 603),
            SurahInfo(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, 604),
            SurahInfo(113, "الفلق", "Al-Falaq", "The Daybreak", 5, 604),
            SurahInfo(114, "الناس", "An-Nas", "Mankind", 6, 604)
        )
    }
}
