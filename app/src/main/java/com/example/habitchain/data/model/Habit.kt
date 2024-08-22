package com.example.habitchain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val frequency: String,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var completedToday: Boolean = false
)