package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdTaskDao {
    @Query("SELECT * FROM ad_tasks ORDER BY timestamp DESC")
    fun getAllAdTasks(): Flow<List<AdTaskRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AdTaskRecordEntity)
}
