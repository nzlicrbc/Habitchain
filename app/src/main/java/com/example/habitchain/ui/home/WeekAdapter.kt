package com.example.habitchain.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.R
import com.example.habitchain.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.*

class WeekAdapter(
    private val weeks: List<List<Calendar>>,
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<WeekAdapter.WeekViewHolder>() {

    private var selectedDate: Calendar = Calendar.getInstance()

    fun selectDate(date: Calendar) {
        selectedDate = date
        notifyDataSetChanged()
        onDateSelected(date)
    }

    inner class WeekViewHolder(private val weekLayout: LinearLayout) : RecyclerView.ViewHolder(weekLayout) {
        private val dayBindings: List<ItemDayBinding> = List(7) {
            ItemDayBinding.inflate(LayoutInflater.from(weekLayout.context), weekLayout, false)
        }

        init {
            dayBindings.forEach { binding ->
                weekLayout.addView(binding.root)
            }
        }

        fun bind(week: List<Calendar>) {
            week.forEachIndexed { index, date ->
                val binding = dayBindings[index]
                val dateFormatter = SimpleDateFormat("d", Locale.getDefault())
                val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())

                binding.textViewDay.text = dayFormatter.format(date.time)
                binding.textViewDate.text = dateFormatter.format(date.time)

                updateDayViewAppearance(binding, date)

                binding.root.setOnClickListener {
                    selectDate(date)
                }
            }
        }

        private fun updateDayViewAppearance(binding: ItemDayBinding, date: Calendar) {
            val isSelected = selectedDate.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                    selectedDate.get(Calendar.YEAR) == date.get(Calendar.YEAR)
            val isToday = Calendar.getInstance().let { today ->
                date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                        date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            }

            when {
                isSelected -> {
                    binding.textViewDate.setBackgroundResource(R.drawable.bg_selected_day)
                    binding.textViewDate.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
                }
                isToday -> {
                    binding.textViewDate.setBackgroundResource(R.drawable.bg_today)
                    binding.textViewDate.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
                }
                else -> {
                    binding.textViewDate.setBackgroundResource(R.drawable.bg_unselected_day)
                    binding.textViewDate.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val weekLayout = LinearLayout(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1f
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 7f
        }
        return WeekViewHolder(weekLayout)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bind(weeks[position])
    }

    override fun getItemCount(): Int = weeks.size
}