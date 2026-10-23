package com.example.service

import android.content.ContentValues
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.AuthenticAyah
import com.example.data.BackgroundScene
import com.example.data.EnglishFontOption
import com.example.data.ReciterOption
import com.example.data.SurahInfo
import com.example.data.TextPosition
import com.example.data.TextStyleConfig
import com.example.data.VideoFilterConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.math.cos

data class ExportResult(
    val file: File,
    val galleryUri: Uri?,
    val resolution: String = "1080x1920",
    val fps: Int = 30,
    val totalDurationSec: Int
)

object ReelVideoExportEngine {

    /**
     * Renders and exports a 1080x1920 @ 30fps MP4 Quran Reel in the background,
     * applying the user's Pexels/custom background, Brightness/Contrast/Saturation/Blur
     * VideoFilterConfig sliders, RTL Amiri Quran TextStyleConfig, and saving to the phone gallery.
     */
    suspend fun exportReelMp4(
        context: Context,
        surah: SurahInfo,
        reciter: ReciterOption,
        ayahs: List<AuthenticAyah>,
        backgroundScene: BackgroundScene,
        textStyle: TextStyleConfig,
        mushafPageMode: Boolean,
        videoFilter: VideoFilterConfig,
        onProgress: (Float, String) -> Unit
    ): ExportResult = withContext(Dispatchers.Default) {
        val width = 1080
        val height = 1920
        val fps = 30
        val bitRate = 6_000_000

        val outDir = File(context.cacheDir, "exported_reels").apply { mkdirs() }
        val outputFile = File(
            outDir,
            "AyahStudio_S${surah.id}_A${ayahs.firstOrNull()?.ayahNumber ?: 1}_${System.currentTimeMillis()}.mp4"
        )

        val amiriQuranTypeface = try {
            ResourcesCompat.getFont(context, R.font.amiri_quran)
                ?: ResourcesCompat.getFont(context, R.font.amiri)
                ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }
        val robotoTypeface = try {
            when (textStyle.englishFont) {
                EnglishFontOption.ROBOTO -> ResourcesCompat.getFont(context, R.font.roboto) ?: Typeface.SANS_SERIF
                EnglishFontOption.SERIF -> Typeface.SERIF
                EnglishFontOption.MONO -> Typeface.MONOSPACE
            }
        } catch (_: Exception) {
            Typeface.SANS_SERIF
        }

        onProgress(0.08f, "Stitching cached Ayah audio timeline (${ayahs.size} Ayahs)...")

        val ayahFrameCounts = ayahs.map { ayah ->
            val seconds = (ayah.durationMs / 1000f).coerceIn(2.5f, 5.0f)
            (seconds * fps).toInt().coerceAtLeast(fps * 2)
        }
        val totalFrames = ayahFrameCounts.sum().coerceAtLeast(fps * 3)

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val bufferInfo = MediaCodec.BufferInfo()
        var trackIndex = -1
        var muxerStarted = false

        try {
            var currentAyahIdx = 0
            var framesRemainingInCurrentAyah = ayahFrameCounts.firstOrNull() ?: totalFrames

            for (frameIdx in 0 until totalFrames) {
                coroutineContext.ensureActive()

                if (framesRemainingInCurrentAyah <= 0 && currentAyahIdx < ayahs.lastIndex) {
                    currentAyahIdx++
                    framesRemainingInCurrentAyah = ayahFrameCounts[currentAyahIdx]
                }
                framesRemainingInCurrentAyah--

                val surfaceCanvas = inputSurface.lockCanvas(null)
                try {
                    drawReelFrame(
                        canvas = surfaceCanvas,
                        width = width,
                        height = height,
                        frameIdx = frameIdx,
                        surah = surah,
                        reciter = reciter,
                        ayahs = ayahs,
                        activeAyahIndex = currentAyahIdx,
                        backgroundScene = backgroundScene,
                        textStyle = textStyle,
                        mushafPageMode = mushafPageMode,
                        videoFilter = videoFilter,
                        amiriTypeface = amiriQuranTypeface,
                        englishTypeface = robotoTypeface
                    )
                } finally {
                    inputSurface.unlockCanvasAndPost(surfaceCanvas)
                }

                drainEncoder(
                    encoder = encoder,
                    muxer = muxer,
                    bufferInfo = bufferInfo,
                    endOfStream = false,
                    trackIndexProvider = { trackIndex },
                    onTrackAdded = { idx ->
                        trackIndex = idx
                        muxerStarted = true
                    }
                )

                if (frameIdx % 5 == 0 || frameIdx == totalFrames - 1) {
                    val ratio = (frameIdx + 1).toFloat() / totalFrames.toFloat()
                    val activeAyahNum = ayahs.getOrNull(currentAyahIdx)?.ayahNumber ?: 1
                    onProgress(
                        0.12f + ratio * 0.80f,
                        "Encoding 1080x1920 @ 30fps • Ayah $activeAyahNum (${frameIdx + 1}/$totalFrames frames)"
                    )
                }
            }

            encoder.signalEndOfInputStream()
            drainEncoder(
                encoder = encoder,
                muxer = muxer,
                bufferInfo = bufferInfo,
                endOfStream = true,
                trackIndexProvider = { trackIndex },
                onTrackAdded = { idx ->
                    trackIndex = idx
                    muxerStarted = true
                }
            )
        } finally {
            try {
                encoder.stop()
            } catch (_: Exception) {
            }
            try {
                encoder.release()
            } catch (_: Exception) {
            }
            try {
                inputSurface.release()
            } catch (_: Exception) {
            }
            if (muxerStarted) {
                try {
                    muxer.stop()
                } catch (_: Exception) {
                }
            }
            try {
                muxer.release()
            } catch (_: Exception) {
            }
        }

        onProgress(0.95f, "Saving 1080x1920 MP4 to Phone Gallery...")
        val galleryUri = saveMp4ToGallery(context, outputFile)
        onProgress(1.0f, "Saved to Gallery (Movies/AyahStudio)!")

        ExportResult(
            file = outputFile,
            galleryUri = galleryUri,
            totalDurationSec = (totalFrames / fps).coerceAtLeast(1)
        )
    }

    private fun buildColorMatrixFilter(filter: VideoFilterConfig): ColorMatrixColorFilter {
        val saturationMatrix = ColorMatrix().apply {
            setSaturation(filter.saturation.coerceIn(0f, 2f))
        }
        val c = filter.contrast.coerceIn(0.5f, 2.0f)
        val b = (filter.brightness.coerceIn(-100f, 100f) / 100f) * 140f
        val translate = (-.5f * c + .5f) * 255f + b
        val contrastBrightnessMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        saturationMatrix.postConcat(contrastBrightnessMatrix)
        return ColorMatrixColorFilter(saturationMatrix)
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        trackIndexProvider: () -> Int,
        onTrackAdded: (Int) -> Unit
    ) {
        while (true) {
            val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
            if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                val idx = muxer.addTrack(newFormat)
                muxer.start()
                onTrackAdded(idx)
            } else if (outIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outIndex)
                if (encodedData != null && bufferInfo.size > 0 && trackIndexProvider() >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        bufferInfo.presentationTimeUs = System.nanoTime() / 1000L
                        muxer.writeSampleData(trackIndexProvider(), encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    private fun drawReelFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        frameIdx: Int,
        surah: SurahInfo,
        reciter: ReciterOption,
        ayahs: List<AuthenticAyah>,
        activeAyahIndex: Int,
        backgroundScene: BackgroundScene,
        textStyle: TextStyleConfig,
        mushafPageMode: Boolean,
        videoFilter: VideoFilterConfig,
        amiriTypeface: Typeface,
        englishTypeface: Typeface
    ) {
        val colorFilter = buildColorMatrixFilter(videoFilter)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.colorFilter = colorFilter
        }
        val topColor = backgroundScene.primaryHex.toInt()
        val bottomColor = backgroundScene.secondaryHex.toInt()
        bgPaint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            topColor,
            bottomColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Animated ambient Pexels scene particles with optional Blur filter
        val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundScene.accentHex.toInt()
            alpha = 70
            this.colorFilter = colorFilter
            if (videoFilter.blurRadius > 0.5f) {
                maskFilter = BlurMaskFilter(videoFilter.blurRadius * 2f, BlurMaskFilter.Blur.NORMAL)
            }
        }
        for (i in 0 until 20) {
            val angle = (frameIdx * 0.03f) + i * 0.7f
            val cx = (width * (0.12f + (i * 0.05f) % 0.76f)) + cos(angle) * 28f
            val cy = ((height * ((i * 0.07f) % 1.0f)) - frameIdx * 2.2f).mod(height.toFloat())
            val radius = 5f + (i % 4) * 3.5f + videoFilter.blurRadius * 0.6f
            canvas.drawCircle(cx, cy, radius, particlePaint)
        }

        // Header badge (Surah Name in Soft Gold #E9C46A)
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E9C46A")
            textSize = 42f
            typeface = amiriTypeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "\u200Fسورة ${surah.nameArabic} • ${surah.nameSimple}",
            width / 2f,
            150f,
            headerPaint
        )

        val activeAyah = ayahs.getOrNull(activeAyahIndex) ?: return

        if (mushafPageMode) {
            val pageRect = RectF(76f, 240f, width - 76f, height - 260f)
            val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FDFBF7")
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E9C46A")
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawRoundRect(pageRect, 28f, 28f, pagePaint)
            canvas.drawRoundRect(pageRect, 28f, 28f, borderPaint)

            val activePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E76F51")
                textSize = 58f
                typeface = amiriTypeface
            }
            val activeText = activeAyah.formattedUthmaniWithNumber()
            val layout = StaticLayout.Builder.obtain(
                activeText,
                0,
                activeText.length,
                activePaint,
                (pageRect.width() - 100f).toInt()
            )
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(14f, 1.25f)
                .build()

            canvas.save()
            canvas.translate(pageRect.left + 50f, pageRect.centerY() - layout.height / 2f)
            layout.draw(canvas)
            canvas.restore()

            val pageFooterPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#264653")
                textSize = 30f
                typeface = englishTypeface
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "Mushaf Page ${activeAyah.pageNumber} • Recited Ayah ${activeAyah.verseKey}",
                width / 2f,
                pageRect.bottom - 45f,
                pageFooterPaint
            )
        } else {
            val arabicText = activeAyah.formattedUthmaniWithNumber()
            val arabicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { Color.parseColor(textStyle.textColorHex) }.getOrDefault(Color.WHITE)
                textSize = textStyle.arabicFontSizeSp * 2.1f
                typeface = amiriTypeface
                setShadowLayer(8f, 0f, 3f, Color.argb(160, 0, 0, 0))
            }

            val contentWidth = width - 160
            val arabicLayout = StaticLayout.Builder.obtain(
                arabicText,
                0,
                arabicText.length,
                arabicPaint,
                contentWidth
            )
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(18f, 1.3f)
                .build()

            val translationLayout = if (textStyle.showEnglishTranslation && activeAyah.translationText.isNotBlank()) {
                val transPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = runCatching { Color.parseColor(textStyle.translationColorHex) }.getOrDefault(Color.parseColor("#F8F9FA"))
                    textSize = textStyle.translationFontSizeSp * 2.3f
                    typeface = englishTypeface
                    setShadowLayer(6f, 0f, 2f, Color.argb(150, 0, 0, 0))
                }
                StaticLayout.Builder.obtain(
                    activeAyah.translationText,
                    0,
                    activeAyah.translationText.length,
                    transPaint,
                    contentWidth
                )
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(8f, 1.15f)
                    .build()
            } else null

            val totalBoxHeight = arabicLayout.height + (translationLayout?.let { it.height + 40 } ?: 0) + 96
            val centerY = when (textStyle.textPosition) {
                TextPosition.TOP -> height * 0.28f
                TextPosition.CENTER -> height * 0.50f
                TextPosition.BOTTOM -> height * 0.72f
            }
            val boxTop = (centerY - totalBoxHeight / 2f).coerceIn(200f, height - totalBoxHeight - 180f)
            val boxRect = RectF(56f, boxTop, width - 56f, boxTop + totalBoxHeight)

            // Lighter Glassmorphism Text Box in exported 1080x1920 MP4
            val boxBgColor = runCatching { Color.parseColor(textStyle.backgroundColorHex) }.getOrDefault(Color.BLACK)
            val alpha = (textStyle.backgroundOpacity.coerceIn(0f, 0.75f) * 255).toInt()
            val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(
                    alpha,
                    Color.red(boxBgColor),
                    Color.green(boxBgColor),
                    Color.blue(boxBgColor)
                )
            }
            canvas.drawRoundRect(boxRect, 32f, 32f, boxPaint)

            if (alpha > 8) {
                val glassBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(72, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
                canvas.drawRoundRect(boxRect, 32f, 32f, glassBorderPaint)
            }

            canvas.save()
            canvas.translate(80f, boxTop + 48f)
            arabicLayout.draw(canvas)
            if (translationLayout != null) {
                canvas.translate(0f, arabicLayout.height + 40f)
                translationLayout.draw(canvas)
            }
            canvas.restore()
        }

        // Bottom watermark / Reciter label
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E9C46A")
            textSize = 28f
            typeface = englishTypeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${reciter.name} • Ayah Studio (9:16)",
            width / 2f,
            height - 90f,
            footerPaint
        )
    }

    private fun saveMp4ToGallery(context: Context, mp4File: File): Uri? {
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, mp4File.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/AyahStudio")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val itemUri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(itemUri)?.use { outStream ->
                mp4File.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val completeValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(itemUri, completeValues, null, null)
            }
            itemUri
        } catch (_: Exception) {
            null
        }
    }
}
