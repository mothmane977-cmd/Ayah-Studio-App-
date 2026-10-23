package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "PexelsRepository"

/**
 * Integrates the official Pexels Videos API (`https://api.pexels.com/videos/search`)
 * for searching vertical 9:16 background videos by keyword (e.g., "Night Sky", "Ocean",
 * "Nature", "Clouds", "Rain", "Desert", "Space").
 *
 * Uses `BuildConfig.PEXELS_API_KEY` when configured via the AI Studio Secrets panel,
 * and seamlessly falls back to the curated Pexels video catalog when no key is set or offline.
 */
class PexelsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchPexelsVideos(query: String): List<BackgroundScene> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim().ifBlank { "night nature" }
        val apiKey = runCatching { BuildConfig.PEXELS_API_KEY }.getOrDefault("")
        val hasValidApiKey = apiKey.isNotBlank() &&
            apiKey != "YOUR_PEXELS_API_KEY" &&
            !apiKey.startsWith("PLACEHOLDER")

        if (hasValidApiKey) {
            try {
                val url = "https://api.pexels.com/videos/search?query=${
                    java.net.URLEncoder.encode(trimmedQuery, "UTF-8")
                }&orientation=portrait&per_page=18"
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", apiKey)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string().orEmpty()
                        val json = JSONObject(bodyStr)
                        val videosArr = json.optJSONArray("videos")
                        if (videosArr != null && videosArr.length() > 0) {
                            val results = mutableListOf<BackgroundScene>()
                            for (i in 0 until videosArr.length()) {
                                val vObj = videosArr.getJSONObject(i)
                                val id = vObj.getInt("id")
                                val durationSec = vObj.optInt("duration", 15)
                                val image = vObj.optString("image")
                                val userObj = vObj.optJSONObject("user")
                                val authorName = userObj?.optString("name", "Pexels") ?: "Pexels"

                                // Extract HD portrait MP4 stream link
                                var streamUrl: String? = null
                                val filesArr = vObj.optJSONArray("video_files")
                                if (filesArr != null) {
                                    for (j in 0 until filesArr.length()) {
                                        val fObj = filesArr.getJSONObject(j)
                                        val fileType = fObj.optString("file_type")
                                        if (fileType == "video/mp4") {
                                            streamUrl = fObj.optString("link")
                                            break
                                        }
                                    }
                                }

                                val mins = durationSec / 60
                                val secs = durationSec % 60
                                val durLabel = "%d:%02d".format(mins, secs)

                                results.add(
                                    BackgroundScene(
                                        videoId = id,
                                        title = "${trimmedQuery.replaceFirstChar { it.uppercase() }} #${i + 1}",
                                        category = trimmedQuery.replaceFirstChar { it.uppercase() },
                                        durationLabel = durLabel,
                                        videographer = "Pexels • $authorName",
                                        themeType = BackgroundThemeType.entries[i % BackgroundThemeType.entries.size],
                                        primaryHex = 0xFF0A1128,
                                        secondaryHex = 0xFF1C2541,
                                        accentHex = 0xFF00F5D4,
                                        thumbnailUrl = image.ifBlank { null },
                                        videoStreamUrl = streamUrl
                                    )
                                )
                            }
                            if (results.isNotEmpty()) return@withContext results
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pexels live API search fell back to curated Pexels catalog: ${e.message}")
            }
        }

        // Filter curated Pexels catalog or generate matching Pexels search items for any query
        val curatedMatches = BackgroundScene.PEXELS_CURATED_PACK.filter {
            query.isBlank() ||
                it.title.contains(trimmedQuery, ignoreCase = true) ||
                it.category.contains(trimmedQuery, ignoreCase = true) ||
                it.videographer.contains(trimmedQuery, ignoreCase = true)
        }
        curatedMatches.ifEmpty {
            BackgroundScene.PEXELS_CURATED_PACK
        }
    }
}
