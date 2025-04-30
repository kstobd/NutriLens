package com.example.theenergyvalue

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()
        
    private val _nutritionData = MutableStateFlow<NutritionData?>(null)
    val nutritionData: StateFlow<NutritionData?> = _nutritionData.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )
    
    private val gson = Gson()
    
    // Инициализация репозитория для работы с историей
    private val repository: HistoryRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.historyDao()
        repository = HistoryRepository(dao)
    }

    fun sendPrompt(
        bitmap: Bitmap
    ) {
        _uiState.value = UiState.Loading
        _nutritionData.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = "Проанализируй изображение пищи и верни информацию о КБЖУ (калории, белки, жиры, углеводы), а также о примерном весе продукта в граммах, в формате JSON. Например: {\"название\": \"Блюдо\", \"калории\": 250, \"белки\": 10, \"жиры\": 5, \"углеводы\": 30, \"вес\": 200}. Используй строго указанный формат JSON без дополнительного текста."
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                response.text?.let { outputContent ->
                    try {
                        // Ищем JSON в ответе (на случай, если модель вернула дополнительный текст)
                        val jsonStart = outputContent.indexOf("{")
                        val jsonEnd = outputContent.lastIndexOf("}") + 1
                        
                        if (jsonStart >= 0 && jsonEnd > jsonStart) {
                            val jsonContent = outputContent.substring(jsonStart, jsonEnd)
                            try {
                                val nutrition = gson.fromJson(jsonContent, NutritionData::class.java)
                                _nutritionData.value = nutrition
                                _uiState.value = UiState.Success(outputContent)
                                
                                // Сохранение результата в историю
                                saveToHistory(nutrition)
                            } catch (e: JsonSyntaxException) {
                                // Обработка случая, когда фрагмент похож на JSON, но не соответствует структуре NutritionData
                                val errorMessage = "Продукт не распознан или формат данных некорректен. Ответ модели: ${outputContent.take(200)}${if (outputContent.length > 200) "..." else ""}"
                                _uiState.value = UiState.Error(errorMessage)
                            }
                        } else {
                            // Если модель не вернула JSON, покажем её текстовый ответ пользователю
                            val errorMessage = "Не удалось распознать продукт. Ответ модели: ${outputContent.take(200)}${if (outputContent.length > 200) "..." else ""}"
                            _uiState.value = UiState.Error(errorMessage)
                        }
                    } catch (e: Exception) {
                        _uiState.value = UiState.Error("Ошибка при обработке ответа: ${e.message ?: "Неизвестная ошибка"}")
                    }
                } ?: run {
                    // Если response.text равен null
                    _uiState.value = UiState.Error("Получен пустой ответ от модели")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("User location is not supported for the API use") == true -> 
                        "Ошибка API: Ваше местоположение не поддерживается для использования API Gemini. " +
                        "Попробуйте использовать VPN для подключения к поддерживаемому региону."
                    else -> "Ошибка при обращении к Gemini API: ${e.localizedMessage ?: "Неизвестная ошибка"}"
                }
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }
    
    // Сохранение данных в историю
    private fun saveToHistory(nutrition: NutritionData) {
        viewModelScope.launch {
            repository.insertFromNutritionData(nutrition)
        }
    }
}