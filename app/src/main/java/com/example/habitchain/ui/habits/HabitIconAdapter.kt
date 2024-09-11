package com.example.habitchain.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.databinding.ItemHabitIconBinding

class HabitIconAdapter(
    private val onIconSelected: (String) -> Unit,
    private val selectedIcon: () -> String
) : ListAdapter<String, HabitIconAdapter.IconViewHolder>(IconDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding =
            ItemHabitIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IconViewHolder(private val binding: ItemHabitIconBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(iconName: String) {
            if (iconName.length == 2) {
                binding.textViewIcon.text = iconName
                binding.textViewIcon.visibility = android.view.View.VISIBLE
                binding.imageViewIcon.visibility = android.view.View.GONE
            } else {
                val resourceId = binding.root.context.resources.getIdentifier(
                    iconName, "drawable", binding.root.context.packageName
                )
                binding.imageViewIcon.setImageResource(resourceId)
                binding.imageViewIcon.visibility = android.view.View.VISIBLE
                binding.textViewIcon.visibility = android.view.View.GONE
            }
            binding.root.isSelected = (iconName == selectedIcon())
            binding.root.setOnClickListener { onIconSelected(iconName) }
        }
    }

    class IconDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }
}