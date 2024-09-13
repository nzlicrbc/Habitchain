package com.example.habitchain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.ui.main.MainActivity
import com.example.habitchain.utils.Constants.CHANNEL_ID
import com.example.habitchain.utils.Constants.HABIT_CHANNEL_DESCRIPTION
import com.example.habitchain.utils.Constants.HABIT_CHANNEL_NAME
import com.example.habitchain.utils.Constants.HABIT_ID
import com.example.habitchain.utils.Constants.HABIT_NAME
import com.example.habitchain.utils.Constants.HABIT_REMINDER_MESSAGE
import com.example.habitchain.utils.Constants.HABIT_REMINDER_TIME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val habitId = inputData.getInt(HABIT_ID, -1)
        val habitName = inputData.getString(HABIT_NAME) ?: return@withContext Result.failure()
        val reminderMessage = inputData.getString(HABIT_REMINDER_MESSAGE) ?: ""
        val reminderTime = inputData.getString(HABIT_REMINDER_TIME) ?: ""

        if (habitId == -1) {
            return@withContext Result.failure()
        }

        val habit = habitRepository.getHabitById(habitId)
        if (habit == null || habit.isCompleted) {
            return@withContext Result.success()
        }

        createNotification(habitId, habitName, reminderMessage, reminderTime)
        Result.success()
    }

    private fun createNotification(
        habitId: Int,
        habitName: String,
        reminderMessage: String,
        reminderTime: String
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                HABIT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = HABIT_CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Habit Reminder: $habitName")
            .setContentText("$reminderMessage (Scheduled for $reminderTime)")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(habitId, notification)
    }
}