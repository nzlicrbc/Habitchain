package com.example.habitchain.ui.habits

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.databinding.ItemHabitColorBinding

class HabitColorAdapter(
    private val onColorSelected: (String) -> Unit,
    private val selectedColor: () -> String
) : ListAdapter<String, HabitColorAdapter.ColorViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemHabitColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = getItem(position)
        holder.bind(color)
    }

    inner class ColorViewHolder(private val binding: ItemHabitColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(color: String) {
            binding.viewColor.setBackgroundColor(Color.parseColor(color))
            binding.root.isSelected = (color == selectedColor())
            binding.root.setOnClickListener { onColorSelected(color) }
        }
    }

    class ColorDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }
}