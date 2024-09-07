package com.example.habitchain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.habitchain.data.Converters

@Entity(tableName = "habits")
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
    val trackDuring: List<String>,
    @TypeConverters(Converters::class)
    val reminders: List<String>,
    val reminderMessage: String,
    var progress: Int = 0,
    var isCompleted: Boolean = false,
)