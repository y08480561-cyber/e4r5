package com.example.tts

import android.content.Context
import java.io.File

class OfflineTtsManager(private val context: Context) {
    private var nativePtr: Long = 0

    init {
        try {
            System.loadLibrary("sherpa-onnx-jni")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun nativeCreate(
        model: String,
        tokens: String,
        dataDir: String,
        numThreads: Int,
        debug: Int,
        noiseScale: Float,
        noiseScaleW: Float,
        lengthScale: Float
    ): Long

    external fun nativeGenerate(ptr: Long, text: String, sid: Int, speed: Float): FloatArray?
    external fun nativeDestroy(ptr: Long)

    fun initialize() {
        val modelFile = extractAsset("fa_IR-gyro-medium.onnx")
        val tokensFile = extractAsset("tokens.txt")
        val dataDir = File(context.filesDir, "espeak-ng-data").absolutePath

        if (modelFile.exists() && tokensFile.exists()) {
            nativePtr = nativeCreate(
                model = modelFile.absolutePath,
                tokens = tokensFile.absolutePath,
                dataDir = dataDir,
                numThreads = 1,
                debug = 0,
                noiseScale = 0.667f,
                noiseScaleW = 0.8f,
                lengthScale = 1.0f
            )
        }
    }

    fun generate(text: String): FloatArray? {
        if (nativePtr == 0L) return null
        return nativeGenerate(nativePtr, text, 0, 1.0f)
    }

    fun release() {
        if (nativePtr != 0L) {
            nativeDestroy(nativePtr)
            nativePtr = 0L
        }
    }

    private fun extractAsset(filename: String): File {
        val outFile = File(context.filesDir, filename)
        if (!outFile.exists()) {
            context.assets.open(filename).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outFile
    }
}
