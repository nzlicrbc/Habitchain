package com.example.habitchain.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habitchain.data.Converters
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.HabitCompletion
import com.example.habitchain.utils.Constants.DATABASE_VERSION

@Database(entities = [Habit::class, HabitCompletion::class], version = DATABASE_VERSION, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}