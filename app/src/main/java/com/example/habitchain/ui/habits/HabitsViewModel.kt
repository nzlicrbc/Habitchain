package com.example.habitchain.ui.habits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habitchain.data.model.HabitCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor() : ViewModel() {

    private val _categories = MutableLiveData<List<HabitCategory>>()
    val categories: LiveData<List<HabitCategory>> = _categories

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val categoryList = listOf(
            HabitCategory("Sports", "⚽"),
            HabitCategory("Hobby", "🎨"),
            HabitCategory("Mental Health", "🧘"),
            HabitCategory("Home", "🏠"),
            HabitCategory("Career", "💼"),
            HabitCategory("Personal Care", "🚿"),
            HabitCategory("Health", "🍎"),
            HabitCategory("Life", "🌱"),
            HabitCategory("Financial", "💰"),
            HabitCategory("Thought", "💭")
        )
        _categories.value = categoryList
    }
}