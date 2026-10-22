package com.example.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechManager(private val context: Context, private val onInitCallback: (Boolean) -> Unit) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    var isPersianSupported = false
        private set

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("fa", "IR"))
            isPersianSupported = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            onInitCallback(isPersianSupported)
        } else {
            onInitCallback(false)
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (isPersianSupported) {
            tts?.language = Locale("fa", "IR")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fa_utterance")
        } else {
            // Fallback or speak English to demonstrate TTS works, or prompt install
            Log.w("TextToSpeechManager", "Persian not supported, attempting default or English")
            tts?.language = Locale.US
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "en_utterance")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun installVoiceData() {
        try {
            val intent = Intent().apply {
                action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
