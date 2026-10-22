package com.example.tts

sealed class TtsState {
    object Ready : TtsState()
    object Producing : TtsState()
    object Playing : TtsState()
    object Paused : TtsState()
    data class Error(val message: String) : TtsState()
}
