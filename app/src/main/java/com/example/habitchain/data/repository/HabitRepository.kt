package com.example.habitchain.data.repository

import android.util.Log
import com.example.habitchain.data.local.HabitDao
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.HabitCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class HabitRepository @Inject constructor(private val habitDao: HabitDao) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Int): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = withContext(Dispatchers.IO) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habitId: Int) = withContext(Dispatchers.IO) {
        val habit = habitDao.getHabitById(habitId)
        habit?.let { habitDao.deleteHabit(it) }
    }

    suspend fun updateHabitCompletion(habitId: Int, completed: Boolean) {
        withContext(Dispatchers.IO) {
            val habit = habitDao.getHabitById(habitId)
            habit?.let {
                val updatedHabit = it.copy(isCompleted = completed)
                habitDao.updateHabit(updatedHabit)
                if (completed) {
                    addHabitCompletion(habitId)
                } else {
                    removeHabitCompletion(habitId)
                }
            }
        }
    }

    private suspend fun removeHabitCompletion(habitId: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        habitDao.removeHabitCompletionForToday(habitId, todayStart)
    }

    suspend fun getHabitCompletionsForLastWeek(): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis

            val completions = habitDao.getHabitCompletionsForLastWeek(startDate)

            val dayMap = mapOf(
                0 to "Sun", 1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat"
            )

            return@withContext completions.associate { dayMap[it.dayOfWeek]!! to it.completionCount }
                .withDefault { 0 }
                .toMutableMap()
                .apply {
                    dayMap.values.forEach { day ->
                        if (!containsKey(day)) {
                            put(day, 0)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error getting weekly completions", e)
            emptyMap()
        }
    }

    suspend fun addHabitCompletion(habitId: Int) {
        val completion = HabitCompletion(habitId = habitId)
        habitDao.insertHabitCompletion(completion)
    }
}