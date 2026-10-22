package com.example.tts

interface PersianTtsEngine {
    fun initialize()
    fun synthesize(text: String)
    fun play()
    fun pause()
    fun resume()
    fun stop()
    fun release()
}
