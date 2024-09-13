package com.example.habitchain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.habitchain.data.Converters
import com.example.habitchain.utils.Constants.TABLE_NAME_HABITS

@Entity(tableName = TABLE_NAME_HABITS)
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val iconName: String,
    val color: String,
    val goal: Int,
    val unit: String,
    val frequency: String,
    @TypeConverters(Converters::class)
    val trackingDays: List<String>,
    val trackDuring: List<String>,
    @TypeConverters(Converters::class)
    val reminders: List<String>,
    val reminderMessage: String,
    var progress: Int = 0,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
)