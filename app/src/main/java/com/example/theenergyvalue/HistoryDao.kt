package com.example.theenergyvalue

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с историей запросов
 */
@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>
    
    @Insert
    suspend fun insertHistoryItem(historyItem: HistoryItem)
    
    @Delete
    suspend fun deleteHistoryItem(historyItem: HistoryItem)
    
    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}