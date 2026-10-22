package com.example.tts

class UnavailableTtsEngine : PersianTtsEngine {
    override fun initialize() {}
    override fun synthesize(text: String) {}
    override fun play() {}
    override fun pause() {}
    override fun resume() {}
    override fun stop() {}
    override fun release() {}
}
