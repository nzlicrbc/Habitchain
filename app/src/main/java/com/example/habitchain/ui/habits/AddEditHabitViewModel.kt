package com.example.habitchain.ui.habits

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.habitchain.NotificationWorker
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _selectedColor = MutableLiveData<String>()
    val selectedColor: LiveData<String> = _selectedColor

    private val _selectedIcon = MutableLiveData<String>()
    val selectedIcon: LiveData<String> = _selectedIcon

    private val _reminders = MutableLiveData<List<String>>()
    val reminders: LiveData<List<String>> = _reminders

    private val _saveComplete = MutableLiveData<Boolean>()
    val saveComplete: LiveData<Boolean> = _saveComplete

    private val _habitAdded = MutableSharedFlow<Habit>()
    val habitAdded = _habitAdded.asSharedFlow()

    private val _habit = MutableLiveData<Habit?>()
    val habit: MutableLiveData<Habit?> = _habit

    init {
        _reminders.value = emptyList()
        _selectedIcon.value = "ic_habit_default"
        _selectedColor.value = "#FFFFFF"
    }

    fun setSelectedColor(color: String) {
        _selectedColor.value = color
    }

    fun setSelectedIcon(icon: String) {
        _selectedIcon.value = icon
    }

    fun addReminder(time: String) {
        val currentReminders = _reminders.value ?: emptyList()
        _reminders.value = currentReminders + time
    }

    fun removeReminder(time: String) {
        val currentReminders = _reminders.value ?: emptyList()
        _reminders.value = currentReminders - time
    }

    fun setReminders(newReminders: List<String>) {
        _reminders.value = newReminders
    }

    private fun scheduleReminders(habit: Habit) {
        habit.reminders.forEach { reminderTime ->
            val (hour, minute) = reminderTime.split(":").map { it.toInt() }
            val initialDelay = calculateInitialDelay(hour, minute)

            val reminderData = workDataOf(
                "habitId" to habit.id,
                "habitName" to habit.name,
                "reminderMessage" to habit.reminderMessage,
                "reminderTime" to reminderTime
            )

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(reminderData)
                .addTag("reminder_${habit.id}")
                .build()

            workManager.enqueueUniqueWork(
                "reminder_${habit.id}_$reminderTime",
                ExistingWorkPolicy.REPLACE,
                request
            )

            Log.d(
                "AddEditHabitViewModel",
                "Scheduled reminder for habit: ${habit.name} at $reminderTime with initial delay: $initialDelay ms"
            )
        }
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        return targetTime.timeInMillis - currentTime.timeInMillis
    }

    private fun cancelReminders(habitId: Int) {
        workManager.cancelAllWorkByTag("reminder_$habitId")
        Log.d("AddEditHabitViewModel", "Cancelled all reminders for habit: $habitId")
    }


    fun saveHabit(
        name: String,
        category: String,
        goal: Int,
        unit: String,
        frequency: String,
        trackDuring: List<String>,
        reminderMessage: String
    ) {
        viewModelScope.launch {
            try {
                val habit = Habit(
                    name = name,
                    category = category,
                    iconName = selectedIcon.value ?: "ic_habit_default",
                    color = selectedColor.value ?: "#FFFFFF",
                    goal = goal,
                    unit = unit,
                    frequency = frequency,
                    trackDuring = trackDuring,
                    reminders = _reminders.value ?: emptyList(),
                    reminderMessage = reminderMessage
                )
                val insertedId = habitRepository.insertHabit(habit)
                val savedHabit = habit.copy(id = insertedId.toInt())
                scheduleReminders(savedHabit)
                Log.d("AddEditHabitViewModel", "Habit inserted with id: $insertedId")
                _saveComplete.value = true
                _habitAdded.emit(savedHabit)
            } catch (e: Exception) {
                Log.e("AddEditHabitViewModel", "Error saving habit", e)
                _saveComplete.value = false
            }
        }
    }

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            try {
                val loadedHabit = habitRepository.getHabitById(habitId)
                _habit.value = loadedHabit
                if (loadedHabit != null) {
                    _selectedColor.value = loadedHabit.color
                    _selectedIcon.value = loadedHabit.iconName
                    _reminders.value = loadedHabit.reminders
                }
            } catch (e: Exception) {
                Log.e("AddEditHabitViewModel", "Error loading habit", e)
            }
        }
    }

    fun updateHabit(
        habitId: Int,
        name: String,
        category: String,
        goal: Int,
        unit: String,
        frequency: String,
        trackDuring: List<String>,
        reminderMessage: String
    ) {
        viewModelScope.launch {
            try {
                val updatedHabit = Habit(
                    id = habitId,
                    name = name,
                    category = category,
                    iconName = selectedIcon.value ?: "ic_habit_default",
                    color = selectedColor.value ?: "#FFFFFF",
                    goal = goal,
                    unit = unit,
                    frequency = frequency,
                    trackDuring = trackDuring,
                    reminders = _reminders.value ?: emptyList(),
                    reminderMessage = reminderMessage
                )
                habitRepository.updateHabit(updatedHabit)
                cancelReminders(habitId)
                scheduleReminders(updatedHabit)
                _saveComplete.value = true
                _habitAdded.emit(updatedHabit)
            } catch (e: Exception) {
                Log.e("AddEditHabitViewModel", "Error updating habit", e)
                _saveComplete.value = false
            }
        }
    }

    fun observeWorkStatus(habitId: Int, lifecycleOwner: LifecycleOwner) {
        workManager.getWorkInfosByTagLiveData("reminder_$habitId")
            .observe(lifecycleOwner) { workInfoList ->
                for (workInfo in workInfoList) {
                    Log.d("WorkManager", "Work state for habit $habitId: ${workInfo.state}")
                }
            }
    }
}