package com.example.habitchain.ui.habits

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.example.habitchain.data.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitAdapterViewModel @Inject internal constructor() : ViewModel() {
    val item: ObservableField<Habit> = ObservableField()
}