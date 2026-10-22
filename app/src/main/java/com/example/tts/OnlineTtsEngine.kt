package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.net.URLEncoder

class OnlineTtsEngine(private val context: Context) : PersianTtsEngine {
    private var mediaPlayer: MediaPlayer? = null

    override fun initialize() {
        // Ready
    }

    override fun synthesize(text: String) {
        if (text.isBlank()) return
        try {
            stop()
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=fa&client=tw-ob"
            
            val headers = mapOf("User-Agent" to "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(url), headers)
                prepareAsync()
                setOnPreparedListener { 
                    it.start() 
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("OnlineTtsEngine", "Playback error: what=$what, extra=$extra")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("OnlineTtsEngine", "Exception in synthesize", e)
            e.printStackTrace()
        }
    }

    override fun play() {}

    override fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun resume() {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        stop()
    }
}
