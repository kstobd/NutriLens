package com.example.theenergyvalue

/**
 * Модель данных для информации о КБЖУ продукта
 */
data class NutritionData(
    val название: String = "Неизвестно",
    val калории: Int = 0,
    val белки: Double = 0.0,
    val жиры: Double = 0.0,
    val углеводы: Double = 0.0,
    val вес: Int = 0 // примерный вес в граммах
)