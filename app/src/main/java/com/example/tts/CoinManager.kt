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

    fun getWatchedAdsCount(): Int {
        return prefs.getInt("watched_ads_count", 0)
    }

    fun setWatchedAdsCount(count: Int) {
        prefs.edit().putInt("watched_ads_count", count).apply()
    }

    fun isUserRegistered(): Boolean {
        return prefs.getBoolean("is_registered", false)
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "کاربر صدایار") ?: "کاربر صدایار"
    }

    fun getUserPhone(): String {
        return prefs.getString("user_phone", "") ?: ""
    }

    fun registerUser(name: String, phone: String) {
        prefs.edit()
            .putBoolean("is_registered", true)
            .putString("user_name", name)
            .putString("user_phone", phone)
            .apply()
    }
}
