package com.example.habitchain.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habitchain.data.Converters
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.HabitCompletion

@Database(entities = [Habit::class, HabitCompletion::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )

                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "Database created")
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            Log.d("AppDatabase", "Destructive migration performed")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabase(context: Context) {
            INSTANCE?.close()
            context.deleteDatabase("habit_database")
            INSTANCE = null
        }
    }
}
