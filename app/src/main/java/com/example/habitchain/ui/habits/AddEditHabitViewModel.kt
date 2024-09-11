package com.example.habitchain.ui.habits

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.habitchain.NotificationWorker
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.repository.HabitRepository
import com.example.habitchain.utils.Constants.HABIT_ID
import com.example.habitchain.utils.Constants.HABIT_NAME
import com.example.habitchain.utils.Constants.HABIT_REMINDER_MESSAGE
import com.example.habitchain.utils.Constants.HABIT_REMINDER_TIME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _habit = MutableLiveData<Habit?>()
    val habit: LiveData<Habit?> = _habit

    private val _selectedColor = MutableLiveData<String>()
    val selectedColor: LiveData<String> = _selectedColor

    private val _selectedIcon = MutableLiveData<String>()
    val selectedIcon: LiveData<String> = _selectedIcon

    private val _reminders = MutableLiveData<List<String>>()
    val reminders: LiveData<List<String>> = _reminders

    private val _trackingDays = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val trackingDays: LiveData<MutableSet<String>> = _trackingDays

    private val _saveComplete = MutableLiveData<Boolean>()
    val saveComplete: LiveData<Boolean> = _saveComplete

    private val _habitAdded = MutableSharedFlow<Habit>()
    val habitAdded = _habitAdded.asSharedFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            _habit.value = habitRepository.getHabitById(habitId)
            _habit.value?.let { habit ->
                _selectedColor.value = habit.color
                _selectedIcon.value = habit.iconName
                _reminders.value = habit.reminders
                _trackingDays.value = habit.trackingDays.toMutableSet()
            }
        }
    }

    fun saveHabit(
        name: String,
        category: String,
        goal: Int,
        unit: String,
        frequency: String,
        trackingDays: List<String>,
        trackDuring: List<String>,
        reminderMessage: String
    ) {
        viewModelScope.launch {
            try {
                val habit = Habit(
                    name = name,
                    category = category,
                    iconName = _selectedIcon.value ?: "",
                    color = _selectedColor.value ?: "",
                    goal = goal,
                    unit = unit,
                    frequency = frequency,
                    trackingDays = trackingDays,
                    trackDuring = trackDuring,
                    reminders = _reminders.value ?: emptyList(),
                    reminderMessage = reminderMessage
                )
                val insertedId = habitRepository.insertHabit(habit)
                val savedHabit = habit.copy(id = insertedId.toInt())
                scheduleReminders(savedHabit)
                _saveComplete.value = true
                _habitAdded.emit(savedHabit)
            } catch (e: Exception) {
                _saveComplete.value = false
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
        trackingDays: List<String>,
        trackDuring: List<String>,
        reminderMessage: String
    ) {
        viewModelScope.launch {
            try {
                val updatedHabit = _habit.value?.copy(
                    name = name,
                    category = category,
                    iconName = _selectedIcon.value ?: "",
                    color = _selectedColor.value ?: "",
                    goal = goal,
                    unit = unit,
                    frequency = frequency,
                    trackingDays = trackingDays,
                    trackDuring = trackDuring,
                    reminders = _reminders.value ?: emptyList(),
                    reminderMessage = reminderMessage
                ) ?: return@launch

                habitRepository.updateHabit(updatedHabit)
                cancelReminders(habitId)
                scheduleReminders(updatedHabit)
                _saveComplete.value = true
                _habitAdded.emit(updatedHabit)
            } catch (e: Exception) {
                _saveComplete.value = false
            }
        }
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

    fun addTrackingDay(day: String) {
        _trackingDays.value?.add(day)
        _trackingDays.value = _trackingDays.value
    }

    fun removeTrackingDay(day: String) {
        _trackingDays.value?.remove(day)
        _trackingDays.value = _trackingDays.value
    }

    fun setTrackingDays(days: List<String>) {
        _trackingDays.value = days.toMutableSet()
    }

    fun getTrackingDays(): List<String> {
        return _trackingDays.value?.toList() ?: emptyList()
    }

    private fun scheduleReminders(habit: Habit) {
        habit.reminders.forEach { reminderTime ->
            val (hour, minute) = reminderTime.split(":").map { it.toInt() }
            val initialDelay = calculateInitialDelay(hour, minute)

            val reminderData = workDataOf(
                HABIT_ID to habit.id,
                HABIT_NAME to habit.name,
                HABIT_REMINDER_MESSAGE to habit.reminderMessage,
                HABIT_REMINDER_TIME to reminderTime
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
    }

    fun observeWorkStatus(habitId: Int, lifecycleOwner: LifecycleOwner) {
        workManager.getWorkInfosByTagLiveData("reminder_$habitId")
            .observe(lifecycleOwner) { workInfoList ->
                for (workInfo in workInfoList) {
                }
            }
    }
}