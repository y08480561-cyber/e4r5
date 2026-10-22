package com.example.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TtsController(private val engine: PersianTtsEngine) {
    private val _state = MutableStateFlow<TtsState>(TtsState.Ready)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    fun initialize() {
        engine.initialize()
    }

    fun synthesize(text: String) {
        if (text.isBlank()) return
        _state.value = TtsState.Producing
        engine.synthesize(text)
    }

    fun play() {
        _state.value = TtsState.Playing
        engine.play()
    }

    fun pause() {
        _state.value = TtsState.Paused
        engine.pause()
    }

    fun resume() {
        _state.value = TtsState.Playing
        engine.resume()
    }

    fun stop() {
        _state.value = TtsState.Ready
        engine.stop()
    }
}
