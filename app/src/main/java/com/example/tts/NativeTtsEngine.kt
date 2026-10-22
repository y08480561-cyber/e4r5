package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class NativeTtsEngine(private val context: Context) : PersianTtsEngine, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false

    override fun initialize() {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("fa", "IR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("NativeTtsEngine", "Persian language not supported on device, falling back to default system locale")
                tts?.language = Locale.getDefault()
            }
            isReady = true
        } else {
            Log.e("NativeTtsEngine", "Initialization failed")
            isReady = false
        }
    }

    override fun synthesize(text: String) {
        if (text.isNotBlank() && isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        }
    }

    override fun play() {}

    override fun pause() {
        tts?.stop()
    }

    override fun resume() {}

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
