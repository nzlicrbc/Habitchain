package com.example.habitchain.ui.stats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.utils.Constants.COMPLETED
import com.example.habitchain.utils.Constants.NOT_DONE
import com.example.habitchain.utils.Constants.STARTED
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
                //Log.e("StatsViewModel", "Error refreshing data", e)
                _weeklyData.postValue(emptyList())
                _stats.postValue(emptyMap())
            }
        }
    }

    private suspend fun updateWeeklyData() {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekData = mutableListOf<Pair<String, Int>>()
        repeat(7) { dayOffset ->
            val date = calendar.time
            val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> ""
            }
            val completedCount = habitRepository.getCompletedHabitsCountForDate(date)
            weekData.add(Pair(dayName, completedCount))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        //Log.d("StatsViewModel", "Weekly data: $weekData")
        _weeklyData.postValue(weekData)
    }

    private suspend fun updateStats() {
        val habits = habitRepository.getAllHabits().first()
        val totalHabits = habits.size
        val completedToday =
            habitRepository.getCompletedHabitsCountForDate(Calendar.getInstance().time)
        val notDoneToday = totalHabits - completedToday

        val stats = mapOf(
            STARTED to totalHabits,
            COMPLETED to completedToday,
            NOT_DONE to notDoneToday
        )
        //Log.d("StatsViewModel", "Updated stats: $stats")
        _stats.postValue(stats)
    }
}