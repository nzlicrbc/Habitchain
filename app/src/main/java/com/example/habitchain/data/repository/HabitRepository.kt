package com.example.habitchain.data.repository

import com.example.habitchain.data.local.HabitDao
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.HabitCompletion
import com.example.habitchain.data.model.DayCompletion
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class HabitRepository @Inject constructor(private val habitDao: HabitDao) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Int): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun getHabitCompletionsForLastWeek(startDate: Long): List<DayCompletion> =
        habitDao.getHabitCompletionsForLastWeek(startDate)

    suspend fun getCompletedHabitsCountForDate(date: Date): Int =
        habitDao.getCompletedHabitsCountForDate(date)

    suspend fun addHabitCompletion(completion: HabitCompletion) =
        habitDao.insertHabitCompletion(completion)

    suspend fun getHabitCompletionsForDateRange(
        startDate: Long,
        endDate: Long
    ): List<HabitCompletion> =
        habitDao.getHabitCompletionsForDateRange(startDate, endDate)

    suspend fun removeHabitCompletionForToday(habitId: Int, todayStart: Long) =
        habitDao.removeHabitCompletionForToday(habitId, todayStart)
}