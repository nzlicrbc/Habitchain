package com.example.habitchain.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.habitchain.utils.Constants.COLUMN_NAME_COMPLETION_DATE
import com.example.habitchain.utils.Constants.COLUMN_NAME_HABIT_ID
import com.example.habitchain.utils.Constants.TABLE_NAME_HABIT_COMPLETIONS

@Entity(tableName = TABLE_NAME_HABIT_COMPLETIONS)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = COLUMN_NAME_HABIT_ID) val habitId: Int,
    @ColumnInfo(name = COLUMN_NAME_COMPLETION_DATE) val completionDate: Long = System.currentTimeMillis()
)