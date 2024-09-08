package com.example.habitchain.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.Quote
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
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

    private var currentFilter = "All"

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
            habitRepository.getAllHabits()
                .catch { e ->
                    _error.value = "Failed to fetch habits: ${e.message}"
                }
                .collect { habitList ->
                    _habits.value = filterHabitsList(habitList)
                }
        }
    }

    fun updateHabitCompletion(habitId: Int, completed: Boolean) {
        viewModelScope.launch {
            try {
                habitRepository.updateHabitCompletion(habitId, completed)
                fetchHabits()
            } catch (e: Exception) {
                _error.value = "Alışkanlık güncellenirken hata oluştu: ${e.message}"
            }
        }
    }

    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val randomQuote = quoteRepository.getRandomQuote()
                _quote.value = randomQuote
            } catch (e: Exception) {
                _error.value = "Failed to fetch quote: ${e.message}"
                _quote.value = Quote("Failed to fetch quote", "Error")
            }
        }
    }

    fun filterHabits(filter: String) {
        currentFilter = filter
        fetchHabits()
    }

    private fun filterHabitsList(habits: List<Habit>): List<Habit> {
        return when (currentFilter) {
            "All" -> habits
            "Active" -> habits.filter { !it.isCompleted }
            "Completed" -> habits.filter { it.isCompleted }
            else -> habits
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                fetchHabits()
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
            }
        }
    }
}