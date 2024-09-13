package com.example.habitchain.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentStatsBinding
import com.example.habitchain.ui.home.WeekAdapter
import com.example.habitchain.utils.Constants.COMPLETED
import com.example.habitchain.utils.Constants.NOT_DONE
import com.example.habitchain.utils.Constants.STARTED
import com.example.habitchain.utils.formatToFullString
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var weekAdapter: WeekAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        setupBarChart()

        viewModel.refreshData()
    }

    private fun setupUI() {
        setupCalendar()
        setupProgressCircle()
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawBarShadow(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter =
                    IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            }

            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false

            legend.isEnabled = false
        }
    }

    private fun setupCalendar() {
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
        binding.textViewCurrentDate.text = selectedDate.time.formatToFullString()
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

    private fun setupProgressCircle() {
        binding.circularProgressBar.progress = 0
        binding.textViewProgressPercentage.text = "0%"
    }

    private fun observeViewModel() {
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            //Log.d("StatsFragment", "Received stats update: $stats")
            updateStats(stats)
        }

        viewModel.weeklyData.observe(viewLifecycleOwner) { weeklyData ->
            //Log.d("StatsFragment", "Received weekly data update: $weeklyData")
            updateBarChart(weeklyData)
        }
    }

    private fun updateStats(stats: Map<String, Int>) {
        binding.textViewStarted.text = stats[STARTED].toString()
        binding.textViewCompleted.text = stats[COMPLETED].toString()
        binding.textViewNotDone.text = stats[NOT_DONE].toString()

        val completed = stats[COMPLETED] ?: 0
        val total = stats[STARTED] ?: 1
        val percentage = if (total > 0) (completed * 100 / total).coerceIn(0, 100) else 0
        binding.circularProgressBar.progress = percentage
        binding.textViewProgressPercentage.text = "$percentage%"
    }

    private fun updateBarChart(weeklyData: List<Pair<String, Int>>) {
        val entries = weeklyData.mapIndexed { index, (_, count) ->
            BarEntry(index.toFloat(), count.toFloat())
        }

        val dataSet = BarDataSet(entries, "Completed Habits").apply {
            color = ContextCompat.getColor(requireContext(), R.color.light_blue)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(weeklyData.map { it.first })
            axisLeft.axisMaximum = (weeklyData.maxOfOrNull { it.second } ?: 0) + 1f
            notifyDataSetChanged()
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}