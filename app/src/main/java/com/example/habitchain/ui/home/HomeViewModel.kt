package com.example.habitchain.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.Quote
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.data.repository.QuoteRepository
import com.example.habitchain.utils.Constants.ERROR_DELETE_HABIT
import com.example.habitchain.utils.Constants.ERROR_FETCH_HABITS
import com.example.habitchain.utils.Constants.ERROR_FETCH_QUOTE
import com.example.habitchain.utils.Constants.FILTER_TEXT_ACTIVE
import com.example.habitchain.utils.Constants.FILTER_TEXT_ALL
import com.example.habitchain.utils.Constants.FILTER_TEXT_COMPLETED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits

    private val _quote = MutableLiveData<Quote>()
    val quote: LiveData<Quote> = _quote

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _selectedDate = MutableLiveData<Calendar>()
    val selectedDate: LiveData<Calendar> = _selectedDate

    private var currentFilter = FILTER_TEXT_ALL

    init {
        fetchHabits()
        fetchQuote()
        _selectedDate.value = Calendar.getInstance()
    }

    fun setSelectedDate(date: Calendar) {
        _selectedDate.value = date
        fetchHabits()
    }

    private fun fetchHabits() {
        viewModelScope.launch {
            try {
                val habits = habitRepository.getAllHabits().first()
                val filteredAndResetHabits = filterAndResetHabits(habits)
                _habits.value = filterHabitsList(filteredAndResetHabits)
            } catch (e: Exception) {
                _error.value = "$ERROR_FETCH_HABITS ${e.message}"
            }
        }
    }

    private suspend fun filterAndResetHabits(habits: List<Habit>): List<Habit> {
        val selectedDate = _selectedDate.value ?: return habits
        val today = Calendar.getInstance()

        return habits.filter { habit ->
            when (habit.frequency) {
                "Day" -> true
                "Week" -> habit.trackingDays.contains(getDayOfWeek(selectedDate))
                "Month" -> habit.trackingDays.contains(
                    selectedDate.get(Calendar.DAY_OF_MONTH).toString()
                )

                else -> false
            }
        }.map { habit ->
            if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            ) {
                habit
            } else {
                val completionsForDate = habitRepository.getHabitCompletionsForDate(selectedDate)
                val isCompletedForSelectedDate = completionsForDate.any { it.habitId == habit.id }
                habit.copy(
                    isCompleted = isCompletedForSelectedDate,
                    currentProgress = if (isCompletedForSelectedDate) habit.goal else 0
                )
            }
        }
    }

    private fun getDayOfWeek(date: Calendar): String {
        return when (date.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> ""
        }
    }

    fun updateHabitCompletion(habitId: Int, completed: Boolean) {
        viewModelScope.launch {
            try {
                habitRepository.updateHabitCompletion(habitId, completed)
                fetchHabits()
            } catch (e: Exception) {
                _error.value = "$ERROR_FETCH_HABITS ${e.message}"
            }
        }
    }

    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val randomQuote = quoteRepository.getRandomQuote()
                _quote.value = randomQuote
            } catch (e: Exception) {
                _error.value = "$ERROR_FETCH_QUOTE: ${e.message}"
                _quote.value = Quote(ERROR_FETCH_QUOTE, "Error")
            }
        }
    }

    fun filterHabits(filter: String) {
        currentFilter = filter
        fetchHabits()
    }

    private fun filterHabitsList(habits: List<Habit>): List<Habit> {
        return when (currentFilter) {
            FILTER_TEXT_ALL -> habits
            FILTER_TEXT_ACTIVE -> habits.filter { !it.isCompleted }
            FILTER_TEXT_COMPLETED -> habits.filter { it.isCompleted }
            else -> habits
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                fetchHabits()
            } catch (e: Exception) {
                _error.value = "$ERROR_DELETE_HABIT: ${e.message}"
            }
        }
    }
}