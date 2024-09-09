package com.example.habitchain.ui.stats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _weeklyData = MutableLiveData<List<Pair<String, Int>>>()
    val weeklyData: LiveData<List<Pair<String, Int>>> = _weeklyData

    private val _stats = MutableLiveData<Map<String, Int>>()
    val stats: LiveData<Map<String, Int>> = _stats

    private val _selectedDate = MutableLiveData<Calendar>()
    val selectedDate: LiveData<Calendar> = _selectedDate

    init {
        refreshData()
    }

    fun setSelectedDate(date: Calendar) {
        _selectedDate.value = date
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                updateWeeklyData()
                updateStats()
            } catch (e: Exception) {
                Log.e("StatsViewModel", "Error refreshing data", e)
                _weeklyData.postValue(emptyList())
                _stats.postValue(emptyMap())
            }
        }
    }

    private suspend fun updateWeeklyData() {
        val weekData = habitRepository.getHabitCompletionsForLastWeek()
        Log.d("StatsViewModel", "Raw weekly data: $weekData")

        val formattedWeekData = weekData.map { (day, count) ->
            day.substring(0, 3).capitalize() to count
        }.sortedBy { getDayOfWeek(it.first) }

        Log.d("StatsViewModel", "Formatted weekly data: $formattedWeekData")
        _weeklyData.postValue(formattedWeekData)
    }

    private suspend fun updateStats() {
        val habits = habitRepository.getAllHabits().first()
        val totalHabits = habits.size
        val completedToday = habits.count { it.isCompleted }
        val notDoneToday = totalHabits - completedToday

        val stats = mapOf(
            "Started" to totalHabits,
            "Completed" to completedToday,
            "NotDone" to notDoneToday
        )
        Log.d("StatsViewModel", "Updated stats: $stats")
        _stats.postValue(stats)
    }

    private fun getDayOfWeek(dayName: String): Int {
        return when (dayName.toLowerCase()) {
            "mon" -> 1
            "tue" -> 2
            "wed" -> 3
            "thu" -> 4
            "fri" -> 5
            "sat" -> 6
            "sun" -> 7
            else -> 0
        }
    }
}