package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.data.AdTaskRecordEntity
import com.example.data.AppDatabase
import com.example.tts.CoinManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val coinManager = CoinManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val adTaskDao = database.adTaskDao()

    var coinBalance by mutableStateOf(coinManager.getCoins())
        private set

    val userName = coinManager.getUserName()
    val userPhone = coinManager.getUserPhone()

    var completedAdTasks by mutableStateOf<List<AdTaskRecordEntity>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            adTaskDao.getAllAdTasks().collectLatest { tasks ->
                completedAdTasks = tasks
            }
        }
    }

    fun refreshData() {
        coinBalance = coinManager.getCoins()
    }
}
