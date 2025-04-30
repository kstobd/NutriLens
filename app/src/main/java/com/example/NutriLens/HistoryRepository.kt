package com.example.NutriLens

import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с историей запросов
 */
class HistoryRepository(private val historyDao: HistoryDao) {
    
    // Получение всей истории запросов
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()
    
    // Добавление нового запроса в историю
    suspend fun insertHistoryItem(historyItem: HistoryItem) {
        historyDao.insertHistoryItem(historyItem)
    }
    
    // Добавление записи из данных о питании
    suspend fun insertFromNutritionData(nutritionData: NutritionData) {
        historyDao.insertHistoryItem(HistoryItem(nutritionData))
    }
    
    // Удаление записи из истории
    suspend fun deleteHistoryItem(historyItem: HistoryItem) {
        historyDao.deleteHistoryItem(historyItem)
    }
    
    // Очистка всей истории запросов
    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}