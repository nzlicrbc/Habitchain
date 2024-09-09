package com.example.habitchain.ui.habits

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habitchain.R
import com.example.habitchain.data.model.Habit
import com.example.habitchain.databinding.FragmentAddEditHabitBinding
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditHabitFragment : Fragment() {

    private lateinit var binding: FragmentAddEditHabitBinding

    private val viewModel: AddEditHabitViewModel by viewModels()
    private val args: AddEditHabitFragmentArgs by navArgs()

    private lateinit var iconAdapter: HabitIconAdapter
    private lateinit var colorAdapter: HabitColorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

        if (args.habitId != -1) {
            viewModel.loadHabit(args.habitId)
        }
    }

    private fun setupUI() {
        binding.textViewCategoryName.text = args.category

        setupFrequencySpinner()
        setupTrackDuringChips()
        setupReminderChips()

        binding.fabAddReminder.setOnClickListener {
            showTimePicker()
        }

        binding.buttonComplete.setOnClickListener {
            if (args.habitId != -1) {
                updateHabit()
            } else {
                saveHabit()
            }
        }

        binding.layoutIconSelection.setOnClickListener { showIconSelectionDialog() }
        binding.layoutColorSelection.setOnClickListener { showColorSelectionDialog() }
    }

    private fun showIconSelectionDialog() {
        val icons = listOf("🏃", "💪", "🏋️", "🚴", "🏊", "🧘", "📚", "🎨", "🎵", "🌱", "🍎", "💧")
        iconAdapter = HabitIconAdapter(
            onIconSelected = {
                viewModel.setSelectedIcon(it)
                updateSelectedIcon(it)
            },
            selectedIcon = { viewModel.selectedIcon.value ?: "" }
        )
        iconAdapter.submitList(icons)

        val dialogView = layoutInflater.inflate(R.layout.dialog_icon_selection, null)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewIcons)
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = iconAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Select Icon")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showColorSelectionDialog() {
        val colors = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
            "#800000", "#008000", "#000080", "#808000", "#800080", "#008080",
            "#FFA500", "#FFC0CB", "#800000", "#FA8072", "#90EE90", "#ADD8E6")
        colorAdapter = HabitColorAdapter(
            onColorSelected = {
                viewModel.setSelectedColor(it)
                updateSelectedColor(it)
            },
            selectedColor = { viewModel.selectedColor.value ?: "" }
        )
        colorAdapter.submitList(colors)

        val dialogView = layoutInflater.inflate(R.layout.dialog_color_selection, null)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewColors)
        recyclerView.layoutManager = GridLayoutManager(context, 6)
        recyclerView.adapter = colorAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Select Color")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateSelectedIcon(icon: String) {
        binding.textViewSelectedIcon.text = icon
    }

    private fun updateSelectedColor(color: String) {
        binding.viewSelectedColor.setBackgroundColor(Color.parseColor(color))
    }

    private fun setupFrequencySpinner() {
        val frequencies = arrayOf("Day", "Week", "Month")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGoalPeriod.adapter = adapter
    }

    private fun setupTrackDuringChips() {
        val trackDuringOptions = listOf("All Day", "Morning", "Afternoon", "Evening")
        trackDuringOptions.forEach { option ->
            val chip = Chip(context).apply {
                text = option
                isCheckable = true
            }
            binding.chipGroupTrackDuring.addView(chip)
        }
    }

    private fun setupReminderChips() {
        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            binding.chipGroupReminders.removeAllViews()
            reminders.forEach { reminder ->
                val chip = Chip(context).apply {
                    text = reminder
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        viewModel.removeReminder(reminder)
                    }
                }
                binding.chipGroupReminders.addView(chip)
            }
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Set reminder time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedTime = String.format("%02d:%02d", picker.hour, picker.minute)
            viewModel.addReminder(selectedTime)
        }

        picker.show(childFragmentManager, "timePicker")
    }

    private fun saveHabit() {
        val name = binding.editTextHabitName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextHabitName.error = "Name cannot be empty"
            return
        }
        val goal = binding.editTextGoal.text.toString().toIntOrNull() ?: 0
        if (goal <= 0) {
            binding.editTextGoal.error = "Goal must be a positive number"
            return
        }
        val unit = binding.editTextUnit.text.toString().trim()
        if (unit.isEmpty()) {
            binding.editTextUnit.error = "Unit cannot be empty"
            return
        }
        val frequency = binding.spinnerGoalPeriod.selectedItem.toString()
        val trackDuring = binding.chipGroupTrackDuring.checkedChipIds.map { id ->
            (binding.chipGroupTrackDuring.findViewById<Chip>(id)).text.toString()
        }
        val reminderMessage = binding.editTextReminderMessage.text.toString().trim()

        viewModel.saveHabit(
            name,
            args.category,
            goal,
            unit,
            frequency,
            trackDuring,
            reminderMessage
        )
    }

    private fun updateHabit() {
        val name = binding.editTextHabitName.text.toString().trim()
        val goal = binding.editTextGoal.text.toString().toIntOrNull() ?: 0
        val unit = binding.editTextUnit.text.toString().trim()
        val frequency = binding.spinnerGoalPeriod.selectedItem.toString()
        val trackDuring = binding.chipGroupTrackDuring.checkedChipIds.map { id ->
            (binding.chipGroupTrackDuring.findViewById<Chip>(id)).text.toString()
        }
        val reminderMessage = binding.editTextReminderMessage.text.toString().trim()

        viewModel.updateHabit(
            args.habitId,
            name,
            args.category,
            goal,
            unit,
            frequency,
            trackDuring,
            reminderMessage
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitAdded.collect { habit ->
                Toast.makeText(
                    context,
                    "Habit '${habit.name}' saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_addEditHabitFragment_to_navigation_home)
            }
        }

        viewModel.saveComplete.observe(viewLifecycleOwner) { saved ->
            if (!saved) {
                Toast.makeText(context, "Error saving habit", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.habit.observe(viewLifecycleOwner) { habit ->
            habit?.let { populateUI(it) }
        }

        viewModel.habit.value?.id?.let { habitId ->
            viewModel.observeWorkStatus(habitId, viewLifecycleOwner)
        }

        viewModel.selectedIcon.observe(viewLifecycleOwner) { icon ->
            updateSelectedIcon(icon)
        }

        viewModel.selectedColor.observe(viewLifecycleOwner) { color ->
            updateSelectedColor(color)
        }
    }

    private fun populateUI(habit: Habit) {
        binding.editTextHabitName.setText(habit.name)
        binding.editTextGoal.setText(habit.goal.toString())
        binding.editTextUnit.setText(habit.unit)
        binding.spinnerGoalPeriod.setSelection(getFrequencyIndex(habit.frequency))

        habit.trackDuring.forEach { trackDuring ->
            val chip = binding.chipGroupTrackDuring.findViewWithTag<Chip>(trackDuring)
            chip?.isChecked = true
        }

        viewModel.setReminders(habit.reminders)

        binding.editTextReminderMessage.setText(habit.reminderMessage)
    }

    private fun getFrequencyIndex(frequency: String): Int {
        return when (frequency) {
            "Day" -> 0
            "Week" -> 1
            "Month" -> 2
            else -> 0
        }
    }
}