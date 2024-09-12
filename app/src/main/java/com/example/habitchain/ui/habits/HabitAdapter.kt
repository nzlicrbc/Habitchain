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
import javax.inject.Inject

class HabitAdapter @Inject constructor() :
    ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    var onItemClicked: ((Habit) -> Unit)? = null
    var onCompletionToggled: ((Habit?, Boolean) -> Unit)? = null
    var onDeleteClicked: ((Habit) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                textViewHabitName.text = habit.name
                updateProgressText(binding, habit, habit.isCompleted)

                viewHabitColor.setBackgroundColor(Color.parseColor(habit.color))

                textViewIcon.text = habit.iconName

                checkBoxHabitCompletion.isChecked = habit.isCompleted

                root.setOnClickListener { onItemClicked?.invoke(habit) }
                checkBoxHabitCompletion.setOnCheckedChangeListener { _, isChecked ->
                    onCompletionToggled?.invoke(habit, isChecked)
                }

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