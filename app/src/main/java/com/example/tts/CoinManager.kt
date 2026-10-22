package com.example.tts

import android.content.Context
import android.content.SharedPreferences

class CoinManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sedayar_coins", Context.MODE_PRIVATE)

    fun getCoins(): Int {
        // Start new users with 4 free initial coins
        return prefs.getInt("coin_balance", 4)
    }

    fun addCoins(amount: Int) {
        val current = getCoins()
        prefs.edit().putInt("coin_balance", current + amount).apply()
    }

    fun deductCoins(amount: Int): Boolean {
        val current = getCoins()
        if (current >= amount) {
            prefs.edit().putInt("coin_balance", current - amount).apply()
            return true
        }
        return false
    }

    fun canClaimDailyReward(): Boolean {
        // Check if reward was already claimed once ever
        return !prefs.getBoolean("has_claimed_one_time_reward", false)
    }

    fun claimDailyReward(): Boolean {
        if (canClaimDailyReward()) {
            addCoins(2)
            prefs.edit().putBoolean("has_claimed_one_time_reward", true).apply()
            return true
        }
        return false
    }
}
