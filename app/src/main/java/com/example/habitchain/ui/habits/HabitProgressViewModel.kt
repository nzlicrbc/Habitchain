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

    private val _habit = MutableLiveData<Habit?>()
    val habit: LiveData<Habit?> = _habit

    private val _updateComplete = MutableLiveData<Boolean>()
    val updateComplete: LiveData<Boolean> = _updateComplete

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            try {
                val loadedHabit = habitRepository.getHabitById(habitId)
                _habit.value = loadedHabit
            } catch (e: Exception) {
            }
        }
    }

    fun updateHabitProgress(progress: Int) {
        viewModelScope.launch {
            try {
                val currentHabit = _habit.value
                if (currentHabit != null) {
                    val updatedHabit = currentHabit.copy(currentProgress = progress)
                    habitRepository.updateHabit(updatedHabit)
                    _updateComplete.value = true
                } else {
                    _updateComplete.value = false
                }
            } catch (e: Exception) {
                _updateComplete.value = false
            }
        }
    }
}