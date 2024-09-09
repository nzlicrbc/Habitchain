package com.example.habitchain.ui.habits

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.data.model.Habit
import com.example.habitchain.databinding.ItemHabitBinding

class HabitAdapter(
    private val onItemClicked: (Habit) -> Unit,
    private val onCompletionToggled: (Habit?, Boolean) -> Unit,
    private val onDeleteClicked: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val viewModel = HabitAdapterViewModel()
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.viewModel = viewModel

        binding.checkBoxHabitCompletion.setOnCheckedChangeListener { _, isChecked ->
            val habit = viewModel.item.get()
            onCompletionToggled(habit, isChecked)
            habit?.let {
                updateProgressText(binding, it, isChecked)
            }
        }
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(habit: Habit) {
            binding.apply {
                viewModel?.item?.set(habit)

                textViewHabitName.text = habit.name
                updateProgressText(binding, habit, habit.isCompleted)

                viewHabitColor.setBackgroundColor(Color.parseColor(habit.color))

                val iconResId = root.context.resources.getIdentifier(
                    habit.iconName, "drawable", root.context.packageName
                )
                imageViewHabitIcon.setImageResource(iconResId)

                checkBoxHabitCompletion.isChecked = habit.isCompleted

                root.setOnClickListener { onItemClicked(habit) }

                executePendingBindings()
            }
        }
    }

    private fun updateProgressText(binding: ItemHabitBinding, habit: Habit, isCompleted: Boolean) {
        val progressText = if (isCompleted) {
            "${habit.goal}/${habit.goal} ${habit.unit}"
        } else {
            "${habit.currentProgress}/${habit.goal} ${habit.unit}"
        }
        binding.textViewHabitProgress.text = progressText
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}