package com.example.habitchain.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager
import com.example.habitchain.data.api.QuoteApiService
import com.example.habitchain.data.local.AppDatabase
import com.example.habitchain.data.local.HabitDao
import com.example.habitchain.ui.habits.HabitAdapter
import com.example.habitchain.utils.Constants.DATABASE_NAME
import com.example.habitchain.utils.Constants.QUOTE_API_BASE_URL
import com.example.habitchain.utils.Constants.SHARED_PREFERENCES_NAME
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitAdapter(): HabitAdapter {
        return HabitAdapter()
    }

    @Provides
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao {
        return appDatabase.habitDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideQuoteApiService(okHttpClient: OkHttpClient): QuoteApiService {
        return Retrofit.Builder()
            .baseUrl(QUOTE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}