package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.tts.network.PiperTtsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RetrofitTtsEngine(private val context: Context) : PersianTtsEngine {
    private val apiService = PiperTtsApiService.instance
    private var mediaPlayer: MediaPlayer? = null
    private var isEnginePlaying = false

    override fun initialize() {
        // Initialization complete
    }

    suspend fun synthesizeAndPlay(text: String) {
        if (text.isBlank()) return
        withContext(Dispatchers.IO) {
            try {
                stop()
                // Google TTS endpoint has a character limit (~100 chars), truncate safely if needed
                val safeText = if (text.length > 100) text.substring(0, 100) else text
                val response = apiService.synthesizeSpeech(text = safeText)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val tempFile = File.createTempFile("retrofit_tts", ".mp3", context.cacheDir)
                        FileOutputStream(tempFile).use { outputStream ->
                            body.byteStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            playFile(tempFile)
                        }
                    }
                } else {
                    Log.e("RetrofitTtsEngine", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("RetrofitTtsEngine", "Exception during synthesis", e)
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
        // Handled via suspend function in ViewModel
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
