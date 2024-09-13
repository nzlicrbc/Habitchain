package com.example.habitchain.data.model

import androidx.room.ColumnInfo
import com.example.habitchain.utils.Constants.COLUMN_INFO_COMPLETION_COUNT
import com.example.habitchain.utils.Constants.COLUMN_INFO_DAY_OF_WEEK

data class DayCompletion(
    @ColumnInfo(name = COLUMN_INFO_DAY_OF_WEEK) val dayOfWeek: Int,
    @ColumnInfo(name = COLUMN_INFO_COMPLETION_COUNT) val completionCount: Int
)