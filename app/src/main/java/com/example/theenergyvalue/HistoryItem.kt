package com.example.theenergyvalue

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Сущность для хранения истории запросов в базе данных
 */
@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "название")
    val name: String,
    
    @ColumnInfo(name = "калории")
    val calories: Int,
    
    @ColumnInfo(name = "белки")
    val proteins: Double,
    
    @ColumnInfo(name = "жиры")
    val fats: Double,
    
    @ColumnInfo(name = "углеводы")
    val carbs: Double,
    
    @ColumnInfo(name = "вес")
    val weight: Int,
    
    val timestamp: Long = System.currentTimeMillis()
) {
    // Конструктор для создания объекта истории из данных о питании
    constructor(nutritionData: NutritionData) : this(
        name = nutritionData.название,
        calories = nutritionData.калории,
        proteins = nutritionData.белки,
        fats = nutritionData.жиры,
        carbs = nutritionData.углеводы,
        weight = nutritionData.вес
    )
    
    // Получение даты записи
    fun getDate(): Date {
        return Date(timestamp)
    }
}