package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.tts.CoinManager
import com.example.tts.RealPersianTtsEngine
import com.example.tts.TransactionManager
import kotlinx.coroutines.launch

enum class VoiceOption(val id: String, val label: String, val gender: String) {
    FEMALE("speaker1", "صدای بانو (خانم)", "زن"),
    MALE("speaker2", "صدای آقا (مرد)", "مرد")
}

data class CoinPack(
    val coins: Int,
    val priceToman: String,
    val title: String,
    val isSpecial: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val engine = RealPersianTtsEngine(application)
    private val coinManager = CoinManager(application)
    private val transactionManager = TransactionManager(application)

    var text by mutableStateOf("")
    var speed by mutableStateOf(0.90f) // Storytelling natural pace
    var selectedVoice by mutableStateOf(VoiceOption.FEMALE)
    
    // Coin & Store States
    var coinBalance by mutableStateOf(coinManager.getCoins())
    var showStoreDialog by mutableStateOf(false)
    var canClaimDaily by mutableStateOf(coinManager.canClaimDailyReward())
    var transactionHistory by mutableStateOf(transactionManager.getTransactions())

    val coinPacks = listOf(
        CoinPack(20, "۵۰,۰۰۰", "بسته ۲۰ سکه"),
        CoinPack(30, "۷۵,۰۰۰", "بسته ۳۰ سکه"),
        CoinPack(50, "۱۲۵,۰۰۰", "بسته ۵۰ سکه"),
        CoinPack(70, "۱۷۵,۰۰۰", "بسته ۷۰ سکه"),
        CoinPack(50, "۱۰۰,۰۰۰", "بسته ویژه پر تخفیف (۵۰ سکه)", isSpecial = true)
    )

    // Status and Progress States
    var statusMessage by mutableStateOf("صدایار آماده است. هر تولید صدا ۲ سکه هزینه دارد.")
    var isSynthesizing by mutableStateOf(false)
    var isSpeaking by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var currentChunk by mutableStateOf(0)
    var totalChunks by mutableStateOf(0)
    var progress by mutableStateOf(0f)

    fun onSpeedChanged(newSpeed: Float) {
        speed = newSpeed
        engine.updatePlaybackSpeed(newSpeed)
    }
    
    fun claimDailyReward() {
        if (coinManager.claimDailyReward()) {
            coinBalance = coinManager.getCoins()
            canClaimDaily = false
            transactionManager.addTransaction("پاداش خوش‌آمدگویی", 2)
            transactionHistory = transactionManager.getTransactions()
            statusMessage = "پاداش خوش‌آمدگویی ۲ سکه با موفقیت دریافت شد!"
        }
    }

    fun speak() {
        if (text.isBlank()) return

        // Deduct 2 coins for generating speech
        if (!coinManager.deductCoins(2)) {
            statusMessage = "موجودی سکه کافی نیست! لطفاً از فروشگاه سکه تهیه کنید."
            showStoreDialog = true
            return
        }
        coinBalance = coinManager.getCoins()
        transactionManager.addTransaction("تولید صدا", -2)
        transactionHistory = transactionManager.getTransactions()

        isSynthesizing = true
        isSpeaking = false
        currentChunk = 0
        totalChunks = 0
        progress = 0f

        viewModelScope.launch {
            engine.synthesizeAndPlay(
                text = text,
                speaker = selectedVoice.id,
                speed = speed
            ) { current, total, playing, message ->
                currentChunk = current
                totalChunks = total
                statusMessage = message
                progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

                if (playing) {
                    isSynthesizing = false
                    isSpeaking = true
                }

                if (message.contains("پایان") || message.contains("خطا") || message.contains("متوقف")) {
                    isSynthesizing = false
                    isSpeaking = false
                }
            }
        }
    }

    fun stop() {
        engine.stop()
        isSynthesizing = false
        isSpeaking = false
        progress = 0f
        statusMessage = "پخش متوقف شد."
    }

    fun saveAudio() {
        if (text.isBlank()) return

        // Deduct 2 coins for saving audio
        if (!coinManager.deductCoins(2)) {
            statusMessage = "موجودی سکه کافی نیست! لطفاً از فروشگاه سکه تهیه کنید."
            showStoreDialog = true
            return
        }
        coinBalance = coinManager.getCoins()
        transactionManager.addTransaction("ذخیره صدا", -2)
        transactionHistory = transactionManager.getTransactions()

        isSaving = true
        currentChunk = 0
        totalChunks = 0
        progress = 0f

        viewModelScope.launch {
            val result = engine.saveAudioToDownloads(
                text = text,
                speaker = selectedVoice.id
            ) { current, total, message ->
                currentChunk = current
                totalChunks = total
                statusMessage = message
                progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
            }

            result.fold(
                onSuccess = { fileName ->
                    statusMessage = "فایل صوتی ($fileName) با موفقیت در پوشه Download ذخیره شد."
                },
                onFailure = { error ->
                    statusMessage = "خطا در ذخیره‌سازی: ${error.message}"
                }
            )
            isSaving = false
        }
    }

    fun buyPack(pack: CoinPack) {
        coinManager.addCoins(pack.coins)
        coinBalance = coinManager.getCoins()
        transactionManager.addTransaction("خرید ${pack.title}", pack.coins)
        transactionHistory = transactionManager.getTransactions()
        statusMessage = "خرید موفق! ${pack.coins} سکه اضافه شد."
        showStoreDialog = false
    }
}
