package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

class OnlineAudioTtsEngine(private val context: Context) : PersianTtsEngine {
    private val client = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null
    private var isEnginePlaying = false

    override fun initialize() {
        // Ready
    }

    suspend fun synthesizeAndPlay(text: String) {
        if (text.isBlank()) return
        withContext(Dispatchers.IO) {
            try {
                stop()
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=fa&client=tw-ob"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://translate.google.com/")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("OnlineAudioTts", "Server error: ${response.code}")
                        return@withContext
                    }

                    val inputStream = response.body?.byteStream() ?: return@withContext
                    val tempFile = File.createTempFile("tts_audio", ".mp3", context.cacheDir)
                    
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    withContext(Dispatchers.Main) {
                        playFile(tempFile)
                    }
                }
            } catch (e: Exception) {
                Log.e("OnlineAudioTts", "Error synthesizing audio", e)
                e.printStackTrace()
            }
        }
    }

    private fun playFile(file: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    isEnginePlaying = false
                    file.delete()
                }
            }
            isEnginePlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun synthesize(text: String) {
        // Handled via synthesizeAndPlay
    }

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
