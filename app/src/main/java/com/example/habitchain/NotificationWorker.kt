package com.example.habitchain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.ui.main.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getInt("habitId", -1)
        val habitName = inputData.getString("habitName") ?: return Result.failure()
        val reminderMessage = inputData.getString("reminderMessage") ?: ""
        val reminderTime = inputData.getString("reminderTime") ?: ""

        Log.d("NotificationWorker", "Preparing notification for habit: $habitName at $reminderTime")

        if (habitId == -1) {
            Log.e("NotificationWorker", "Invalid habitId")
            return Result.failure()
        }

        val habit = habitRepository.getHabitById(habitId)
        if (habit == null || habit.isCompleted) {
            Log.d(
                "NotificationWorker",
                "Habit $habitName is completed or doesn't exist. Skipping notification."
            )
            return Result.success()
        }

        createNotification(habitId, habitName, reminderMessage, reminderTime)
        return Result.success()
    }

    private fun createNotification(
        habitId: Int,
        habitName: String,
        reminderMessage: String,
        reminderTime: String
    ) {
        val builder: NotificationCompat.Builder
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "HabitChainChannel"
            val channelName = "Habit Reminders"
            val channelDescription = "Notifications for habit reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            var channel: NotificationChannel? =
                notificationManager.getNotificationChannel(channelId)

            if (channel == null) {
                channel = NotificationChannel(channelId, channelName, importance)
                channel.description = channelDescription
                notificationManager.createNotificationChannel(channel)
            }

            builder = NotificationCompat.Builder(applicationContext, channelId)
        } else {
            builder = NotificationCompat.Builder(applicationContext)
        }

        builder.setContentTitle("Habit Reminder: $habitName")
            .setContentText("$reminderMessage (Scheduled for $reminderTime)")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .priority = NotificationCompat.PRIORITY_HIGH

        notificationManager.notify(habitId * 10000 + reminderTime.hashCode(), builder.build())
        Log.d("NotificationWorker", "Notification sent for habit: $habitName at $reminderTime")
    }
}