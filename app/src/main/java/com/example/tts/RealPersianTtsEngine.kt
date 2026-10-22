package com.example.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.tts.network.PersianTtsApiService
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class RealPersianTtsEngine(private val context: Context) : PersianTtsEngine {
    private val apiService = PersianTtsApiService.instance
    private val audioFileManager = AudioFileManager(context)
    private var mediaPlayer: MediaPlayer? = null
    private var isEnginePlaying = false
    private var currentJob: Job? = null
    private var isCancelled = false

    private var currentSpeed: Float = 0.90f
    
    // Cached state to ensure fresh generation when needed
    private var lastText: String = ""
    private var lastSpeaker: String = ""
    private val lastGeneratedUrls = mutableListOf<String>()

    override fun initialize() {}

    /**
     * Synthesizes and plays Persian text automatically with detailed progress feedback.
     */
    suspend fun synthesizeAndPlay(
        text: String,
        speaker: String = "speaker1",
        speed: Float = 0.90f,
        onProgressUpdate: ((current: Int, total: Int, isPlaying: Boolean, message: String) -> Unit)? = null
    ) {
        if (text.isBlank()) return
        stop()
        isCancelled = false
        
        lastText = text
        lastSpeaker = speaker
        lastGeneratedUrls.clear()
        currentSpeed = speed

        currentJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Split text into natural vocalized storytelling chunks (<= 34 chars)
                val chunks = PersianTextNormalizer.splitIntoChunks(text, maxLen = 34)
                val totalChunks = chunks.size

                if (totalChunks == 0) {
                    withContext(Dispatchers.Main) {
                        onProgressUpdate?.invoke(0, 0, false, "متن نامعتبر است.")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    onProgressUpdate?.invoke(1, totalChunks, false, "در حال پردازش و تولید صوت بخش ۱ از $totalChunks...")
                }

                val audioUrls = ConcurrentHashMap<Int, String>()
                val failedIndices = ConcurrentHashMap.newKeySet<Int>()

                // Fetch first chunk to begin playing immediately
                val firstSuccess = fetchChunkWithRetry(chunks[0], speaker, 0, audioUrls, failedIndices)
                if (!firstSuccess || isCancelled) {
                    withContext(Dispatchers.Main) {
                        onProgressUpdate?.invoke(0, totalChunks, false, "خطا در برقراری ارتباط با سرور صوت.")
                    }
                    return@launch
                }

                // In background, prefetch all remaining chunks smoothly
                if (totalChunks > 1) {
                    launch {
                        for (i in 1 until totalChunks) {
                            if (isCancelled) break
                            fetchChunkWithRetry(chunks[i], speaker, i, audioUrls, failedIndices)
                            delay(60)
                        }
                    }
                }

                // Play all chunks sequentially with pure 1.0f pitch for 100% natural human quality
                for (i in 0 until totalChunks) {
                    if (isCancelled) break

                    var attempts = 0
                    while (!audioUrls.containsKey(i) && !failedIndices.contains(i) && !isCancelled && attempts < 40) {
                        delay(150)
                        attempts++
                    }

                    if (isCancelled) break

                    val url = audioUrls[i]
                    if (!url.isNullOrBlank()) {
                        synchronized(lastGeneratedUrls) {
                            if (!lastGeneratedUrls.contains(url)) {
                                lastGeneratedUrls.add(url)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            val chunkNumber = i + 1
                            onProgressUpdate?.invoke(
                                chunkNumber,
                                totalChunks,
                                true,
                                "در حال روایت بخش $chunkNumber از $totalChunks..."
                            )
                        }
                        playChunkSync(url, currentSpeed)
                    }
                }

                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        onProgressUpdate?.invoke(totalChunks, totalChunks, false, "پخش با موفقیت به پایان رسید.")
                    }
                }
            } catch (e: Exception) {
                Log.e("RealPersianTtsEngine", "Error in storytelling playback", e)
                withContext(Dispatchers.Main) {
                    onProgressUpdate?.invoke(0, 0, false, "خطا در پردازش متن.")
                }
            }
        }
    }

    fun updatePlaybackSpeed(speed: Float) {
        currentSpeed = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { mp ->
                    if (isEnginePlaying) {
                        mp.playbackParams = PlaybackParams().apply {
                            setSpeed(speed)
                            setPitch(1.0f)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveAudioToDownloads(
        text: String,
        speaker: String = "speaker1",
        onProgressUpdate: ((current: Int, total: Int, message: String) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(Exception("متن برای ذخیره نمی‌تواند خالی باشد."))
        }

        try {
            val urlsToSave = mutableListOf<String>()

            if (lastGeneratedUrls.isNotEmpty() && lastText == text && lastSpeaker == speaker) {
                urlsToSave.addAll(lastGeneratedUrls)
            } else {
                val chunks = PersianTextNormalizer.splitIntoChunks(text, maxLen = 34)
                val totalChunks = chunks.size

                for (i in 0 until totalChunks) {
                    withContext(Dispatchers.Main) {
                        onProgressUpdate?.invoke(i + 1, totalChunks, "در حال آماده‌سازی بخش ${i + 1} از $totalChunks...")
                    }
                    val audioUrls = ConcurrentHashMap<Int, String>()
                    val failedIndices = ConcurrentHashMap.newKeySet<Int>()
                    val success = fetchChunkWithRetry(chunks[i], speaker, i, audioUrls, failedIndices)
                    if (success && audioUrls.containsKey(i)) {
                        urlsToSave.add(audioUrls[i]!!)
                    }
                    delay(50)
                }
            }

            if (urlsToSave.isEmpty()) {
                return@withContext Result.failure(Exception("هیچ قطعه صوتی برای ذخیره‌سازی یافت نشد."))
            }

            withContext(Dispatchers.Main) {
                onProgressUpdate?.invoke(urlsToSave.size, urlsToSave.size, "در حال ذخیره در پوشه دانلود (Download)...")
            }

            val result = audioFileManager.downloadAndSaveAudio(urlsToSave, baseTitle = "Sedayar_Dastani")
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchChunkWithRetry(
        chunkText: String,
        speaker: String,
        index: Int,
        cache: ConcurrentHashMap<Int, String>,
        failed: MutableSet<Int>
    ): Boolean {
        for (attempt in 1..2) {
            try {
                val response = apiService.convert(speaker = speaker, text = chunkText)
                if (response.isSuccessful && !response.body()?.audio.isNullOrBlank()) {
                    cache[index] = response.body()!!.audio!!
                    return true
                }
            } catch (e: Exception) {
                Log.w("RealPersianTtsEngine", "Attempt $attempt failed for chunk $index: ${e.message}")
            }
            if (attempt < 2) delay(200)
        }
        failed.add(index)
        return false
    }

    private suspend fun playChunkSync(url: String, speed: Float) {
        val completable = CompletableDeferred<Unit>()

        withContext(Dispatchers.Main) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = PlaybackParams().apply {
                                    setSpeed(speed)
                                    setPitch(1.0f)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        mp.start()
                        isEnginePlaying = true
                    }
                    setOnCompletionListener {
                        isEnginePlaying = false
                        completable.complete(Unit)
                    }
                    setOnErrorListener { _, _, _ ->
                        isEnginePlaying = false
                        completable.complete(Unit)
                        true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                completable.complete(Unit)
            }
        }

        completable.await()
    }

    override fun synthesize(text: String) {}

    override fun play() {
        if (mediaPlayer != null && !isEnginePlaying) {
            mediaPlayer?.start()
            isEnginePlaying = true
        }
    }

    override fun pause() {
        try {
            mediaPlayer?.pause()
            isEnginePlaying = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun resume() {
        play()
    }

    override fun stop() {
        isCancelled = true
        currentJob?.cancel()
        currentJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isEnginePlaying = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        stop()
    }
}
