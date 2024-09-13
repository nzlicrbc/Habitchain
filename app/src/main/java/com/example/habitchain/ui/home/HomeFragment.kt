package com.example.habitchain.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentHomeBinding
import com.example.habitchain.ui.habits.HabitAdapter
import com.example.habitchain.utils.Constants.FILTER_TEXT_ACTIVE
import com.example.habitchain.utils.Constants.FILTER_TEXT_ALL
import com.example.habitchain.utils.Constants.FILTER_TEXT_COMPLETED
import com.example.habitchain.utils.Constants.TODAY
import com.example.habitchain.utils.SwipeActionCallback
import com.example.habitchain.utils.formatToFullString
import com.example.habitchain.utils.formatToString
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var habitAdapter: HabitAdapter
    private lateinit var weekAdapter: WeekAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.textViewToday.text = Date().formatToFullString()
        setupWeekView()
    }

    private fun setupWeekView() {
        val weeks = createWeeks()
        val today = Calendar.getInstance()
        val currentWeekIndex = weeks.indexOfFirst { week ->
            week.any {
                it.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                        it.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            }
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
        val displayText = if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        ) {
            TODAY
        } else {
            selectedDate.time.formatToString()
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
        habitAdapter.onItemClicked = { habit ->
            val action =
                HomeFragmentDirections.actionNavigationHomeToHabitProgressFragment(habit.id)
            findNavController().navigate(action)
        }
        habitAdapter.onCompletionToggled = { habit, isCompleted ->
            habit?.id?.let { viewModel.updateHabitCompletion(it, isCompleted) }
        }
        habitAdapter.onDeleteClicked = { habit ->
            viewModel.deleteHabit(habit.id)
        }

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
                val action =
                    HomeFragmentDirections.actionNavigationHomeToAddEditHabitFragment(habit.id)
                findNavController().navigate(action)
            }
        )
        val itemTouchHelper = ItemTouchHelper(swipeActionCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHabits)
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.submitList(habits)
            if (habits.isEmpty()) {
                binding.recyclerViewHabits.visibility = View.GONE
                binding.imageViewHome.visibility = View.VISIBLE
                binding.textViewAddText.visibility = View.VISIBLE
            } else {
                binding.recyclerViewHabits.visibility = View.VISIBLE
                binding.imageViewHome.visibility = View.GONE
                binding.textViewAddText.visibility = View.GONE
            }
        }

        viewModel.quote.observe(viewLifecycleOwner) { quote ->
            binding.textViewQuote.text = "${quote.quoteText} - ${quote.quoteAuthor}"
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
                    viewModel.filterHabits(FILTER_TEXT_ALL)
                    binding.textViewAll.text = FILTER_TEXT_ALL
                    true
                }

                R.id.filter_active -> {
                    viewModel.filterHabits(FILTER_TEXT_ACTIVE)
                    binding.textViewAll.text = FILTER_TEXT_ACTIVE
                    true
                }

                R.id.filter_completed -> {
                    viewModel.filterHabits(FILTER_TEXT_COMPLETED)
                    binding.textViewAll.text = FILTER_TEXT_COMPLETED
                    true
                }

                else -> false
            }
        }

        popup.show()
    }
}