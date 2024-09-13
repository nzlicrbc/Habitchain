package com.example.habitchain.ui.habits

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import com.example.habitchain.utils.Constants.AFTERNOON
import com.example.habitchain.utils.Constants.ALL_DAY
import com.example.habitchain.utils.Constants.DAY
import com.example.habitchain.utils.Constants.ERROR_GOAL_INVALID
import com.example.habitchain.utils.Constants.ERROR_NAME_EMPTY
import com.example.habitchain.utils.Constants.ERROR_SAVING_HABIT
import com.example.habitchain.utils.Constants.ERROR_UNIT_EMPTY
import com.example.habitchain.utils.Constants.EVENING
import com.example.habitchain.utils.Constants.MONTH
import com.example.habitchain.utils.Constants.MORNING
import com.example.habitchain.utils.Constants.OKEY
import com.example.habitchain.utils.Constants.SELECT_COLOR
import com.example.habitchain.utils.Constants.SELECT_ICON
import com.example.habitchain.utils.Constants.SUCCESS_HABIT_SAVED
import com.example.habitchain.utils.Constants.TIME_PICKER_TITLE
import com.example.habitchain.utils.Constants.WEEK
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

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

        args.habitId?.let { id ->
            viewModel.loadHabit(id)
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

        setupTrackingDaysChipGroup()
    }

    private fun setupFrequencySpinner() {
        val frequencies = arrayOf(DAY, WEEK, MONTH)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGoalPeriod.adapter = adapter

        binding.spinnerGoalPeriod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (frequencies[position]) {
                        DAY -> binding.chipGroupTrackingDays.visibility = View.GONE
                        WEEK -> {
                            binding.chipGroupTrackingDays.visibility = View.VISIBLE
                            setupWeekDayChips()
                        }
                        MONTH -> {
                            binding.chipGroupTrackingDays.visibility = View.VISIBLE
                            setupMonthDayChips()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupTrackingDaysChipGroup() {
        binding.chipGroupTrackingDays.visibility = View.GONE
    }

    private fun setupWeekDayChips() {
        binding.chipGroupTrackingDays.removeAllViews()
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        weekDays.forEach { day ->
            val chip = createChip(day)
            binding.chipGroupTrackingDays.addView(chip)
        }
    }

    private fun setupMonthDayChips() {
        binding.chipGroupTrackingDays.removeAllViews()
        (1..31).forEach { day ->
            val chip = createChip(day.toString())
            binding.chipGroupTrackingDays.addView(chip)
        }
    }

    private fun createChip(text: String): Chip {
        return Chip(context).apply {
            this.text = text
            isCheckable = true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.addTrackingDay(text)
                } else {
                    viewModel.removeTrackingDay(text)
                }
            }
        }
    }

    private fun showIconSelectionDialog() {
        val icons = listOf(
            "🌞", "💧", "🚰", "🥤", "🚴", "🏊", "👟", "🍇", "🥑", "🍓", "🚀", "🌟", "📈", "💖", "💅",
            "🎊", "🥳", "😟", "😣", "😓", "😊", "😄", "👍", "🙌", "🤝", "🎉", "🧹", "🧽", "🧼", "🎯", "🏹", "💰",
            "📊", "💸", "🌿", "🌱", "🚲", "🌍", "🎨", "✏️", "🎸", "🎶", "💼", "🧺", "🌳", "🌿",
            "🌸", "🛌", "😴", "🌙", "🍏", "🥗", "🍎", "🏋️", "🏃‍♀️", "🚴", "🏊", "📚", "💡", "🧠", "⏰", "🕒",
            "📅", "✔️", "✅", "🏆"
        )

        iconAdapter = HabitIconAdapter(
            onIconSelected = {
                viewModel.setSelectedIcon(it)
                updateSelectedIcon(it)
            },
            selectedIcon = { viewModel.selectedIcon.value ?: "" }
        )
        iconAdapter.submitList(icons)

        val dialogView = layoutInflater.inflate(R.layout.dialog_icon_selection, null)
        val recyclerView =
            dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewIcons)
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = iconAdapter

        AlertDialog.Builder(requireContext())
            .setTitle(SELECT_ICON)
            .setView(dialogView)
            .setPositiveButton(OKEY, null)
            .show()
    }

    private fun showColorSelectionDialog() {
        val colors = listOf(
            "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
            "#800000", "#008000", "#000080", "#808000", "#800080", "#008080",
            "#FFA500", "#FFC0CB", "#800000", "#FA8072", "#90EE90", "#ADD8E6"
        )
        colorAdapter = HabitColorAdapter(
            onColorSelected = {
                viewModel.setSelectedColor(it)
                updateSelectedColor(it)
            },
            selectedColor = { viewModel.selectedColor.value ?: "" }
        )
        colorAdapter.submitList(colors)

        val dialogView = layoutInflater.inflate(R.layout.dialog_color_selection, null)
        val recyclerView =
            dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewColors)
        recyclerView.layoutManager = GridLayoutManager(context, 6)
        recyclerView.adapter = colorAdapter

        AlertDialog.Builder(requireContext())
            .setTitle(SELECT_COLOR)
            .setView(dialogView)
            .setPositiveButton(OKEY, null)
            .show()
    }

    private fun updateSelectedIcon(icon: String) {
        binding.textViewSelectedIcon.text = icon
    }

    private fun updateSelectedColor(color: String) {
        binding.viewSelectedColor.setBackgroundColor(Color.parseColor(color))
    }

    private fun setupTrackDuringChips() {
        val trackDuringOptions = listOf(ALL_DAY, MORNING, AFTERNOON, EVENING)
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
            .setTitleText(TIME_PICKER_TITLE)
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedTime =
                String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            viewModel.addReminder(selectedTime)
        }

        picker.show(childFragmentManager, "timePicker")
    }

    private fun saveHabit() {
        val name = binding.editTextHabitName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextHabitName.error = ERROR_NAME_EMPTY
            return
        }
        val goal = binding.editTextGoal.text.toString().toIntOrNull() ?: 0
        if (goal <= 0) {
            binding.editTextGoal.error = ERROR_GOAL_INVALID
            return
        }
        val unit = binding.editTextUnit.text.toString().trim()
        if (unit.isEmpty()) {
            binding.editTextUnit.error = ERROR_UNIT_EMPTY
            return
        }
        val frequency = binding.spinnerGoalPeriod.selectedItem.toString()
        val trackDuring = binding.chipGroupTrackDuring.checkedChipIds.map { id ->
            (binding.chipGroupTrackDuring.findViewById<Chip>(id)).text.toString()
        }
        val reminderMessage = binding.editTextReminderMessage.text.toString().trim()

        val trackingDays = viewModel.getTrackingDays()

        viewModel.saveHabit(
            name,
            args.category,
            goal,
            unit,
            frequency,
            trackingDays,
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

        val trackingDays = viewModel.getTrackingDays()

        viewModel.updateHabit(
            args.habitId!!,
            name,
            args.category,
            goal,
            unit,
            frequency,
            trackingDays,
            trackDuring,
            reminderMessage
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitAdded.collect { habit ->
                Toast.makeText(
                    context,
                    SUCCESS_HABIT_SAVED.format(habit.name),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_addEditHabitFragment_to_navigation_home)
            }
        }

        viewModel.saveComplete.observe(viewLifecycleOwner) { saved ->
            if (!saved) {
                Toast.makeText(context, ERROR_SAVING_HABIT, Toast.LENGTH_SHORT).show()
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

    private fun getFrequencyIndex(frequency: String): Int {
        return when (frequency) {
            DAY -> 0
            WEEK -> 1
            MONTH -> 2
            else -> 0
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
        viewModel.setTrackingDays(habit.trackingDays)

        when (habit.frequency) {
            WEEK -> setupWeekDayChips()
            MONTH -> setupMonthDayChips()
            else -> binding.chipGroupTrackingDays.visibility = View.GONE
        }

        habit.trackingDays.forEach { day ->
            binding.chipGroupTrackingDays.findViewWithTag<Chip>(day)?.isChecked = true
        }

        binding.editTextReminderMessage.setText(habit.reminderMessage)
    }
}