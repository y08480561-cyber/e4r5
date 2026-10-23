package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_tasks")
data class AdTaskRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskTitle: String,
    val rewardCoins: Int,
    val timestamp: Long = System.currentTimeMillis()
)
