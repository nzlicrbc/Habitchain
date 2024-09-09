package com.example.habitchain

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.habitchain.data.local.AppDatabase
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitChainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            resetAndInitializeDatabase()
        } catch (e: Exception) {
            Log.e("HabitChainApplication", "Error during application initialization", e)
        }
    }

    private fun resetAndInitializeDatabase() {
        try {
            AppDatabase.resetDatabase(this)
            val db = AppDatabase.getDatabase(this)
            db.openHelper.writableDatabase
            Log.d("HabitChainApplication", "Database reset and initialized successfully")
        } catch (e: Exception) {
            Log.e("HabitChainApplication", "Error resetting and initializing database", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}