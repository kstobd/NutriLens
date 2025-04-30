package com.example.NutriLens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для работы с историей запросов
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository
    val allHistory: StateFlow<List<HistoryItem>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.historyDao()
        repository = HistoryRepository(dao)
        
        allHistory = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    // Удаление отдельной записи из истории
    fun deleteItem(historyItem: HistoryItem) {
        viewModelScope.launch {
            repository.deleteHistoryItem(historyItem)
        }
    }
    
    // Очистка всей истории
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}