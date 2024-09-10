package com.example.habitchain.ui.habits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitProgressViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _habit = MutableLiveData<Habit>()
    val habit: LiveData<Habit> = _habit

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            habitRepository.getHabitById(habitId)?.let {
                _habit.value = it
            }
        }
    }

    fun incrementProgress() {
        _habit.value?.let { habit ->
            updateProgress(habit.currentProgress + 1)
        }
    }

    fun decrementProgress() {
        _habit.value?.let { habit ->
            updateProgress(habit.currentProgress - 1)
        }
    }

    fun resetProgress() {
        updateProgress(0)
    }

    fun updateProgress(progress: Int) {
        _habit.value?.let { habit ->
            val updatedProgress = progress.coerceIn(0, habit.goal)
            val updatedHabit = habit.copy(
                currentProgress = updatedProgress,
                progress = updatedProgress,
                isCompleted = updatedProgress >= habit.goal
            )
            viewModelScope.launch {
                habitRepository.updateHabit(updatedHabit)
                _habit.value = updatedHabit
            }
        }
    }
}