package com.example.habitchain.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentHomeBinding
import com.example.habitchain.ui.habits.HabitAdapter
import com.example.habitchain.utils.SwipeActionCallback
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var weekAdapter: WeekAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupUI() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        binding.textViewToday.text = dateFormat.format(Date())

        setupWeekView()
    }

    private fun setupWeekView() {
        val weeks = createWeeks()
        val today = Calendar.getInstance()
        val currentWeekIndex = weeks.indexOfFirst { week ->
            week.any { it.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    it.get(Calendar.YEAR) == today.get(Calendar.YEAR) }
        }

        weekAdapter = WeekAdapter(weeks) { selectedDate ->
            updateDateDisplay(selectedDate)
            viewModel.setSelectedDate(selectedDate)
        }

        binding.viewPagerWeeks.apply {
            adapter = weekAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            setCurrentItem(currentWeekIndex, false)
        }

        weekAdapter.selectDate(today)
        updateDateDisplay(today)
    }

    private fun updateDateDisplay(selectedDate: Calendar) {
        val today = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())

        val displayText = if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            "Today"
        } else {
            dateFormat.format(selectedDate.time)
        }

        binding.textViewToday.text = displayText
    }


    private fun createWeeks(): List<List<Calendar>> {
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }

        calendar.add(Calendar.WEEK_OF_YEAR, -2)

        return List(5) { weekIndex ->
            List(7) { dayIndex ->
                val day = calendar.clone() as Calendar
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                day
            }
        }
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onItemClicked = { habit ->
                val action = HomeFragmentDirections.actionNavigationHomeToHabitProgressFragment(habit.id)
                findNavController().navigate(action)
            },
            onCompletionToggled = { habit, isCompleted ->
                habit?.id?.let { viewModel.updateHabitCompletion(it, isCompleted) }
            },
            onDeleteClicked = { habit ->
                viewModel.deleteHabit(habit.id)
            }
        )
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }

        val swipeActionCallback = SwipeActionCallback(
            context = requireContext(),
            onDeleteClicked = { position ->
                val habit = habitAdapter.currentList[position]
                viewModel.deleteHabit(habit.id)
            },
            onEditClicked = { position ->
                val habit = habitAdapter.currentList[position]
                val action = HomeFragmentDirections.actionNavigationHomeToAddEditHabitFragment(habit.id)
                findNavController().navigate(action)
            }
        )
        val itemTouchHelper = ItemTouchHelper(swipeActionCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHabits)

        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.submitList(habits)
        }
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.submitList(habits)
            binding.recyclerViewHabits.visibility = if (habits.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.quote.observe(viewLifecycleOwner) { quote ->
            binding.textViewQuote.text = "${quote.text} - ${quote.author}"
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_habitsFragment)
        }

        binding.textViewAll.setOnClickListener { view ->
            showFilterMenu(view)
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.habit_filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.filter_all -> {
                    viewModel.filterHabits("All")
                    binding.textViewAll.text = "All"
                    true
                }
                R.id.filter_active -> {
                    viewModel.filterHabits("Active")
                    binding.textViewAll.text = "Active"
                    true
                }
                R.id.filter_completed -> {
                    viewModel.filterHabits("Completed")
                    binding.textViewAll.text = "Completed"
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}