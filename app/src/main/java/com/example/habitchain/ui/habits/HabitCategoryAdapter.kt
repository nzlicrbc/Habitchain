package com.example.habitchain.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.data.model.HabitCategory
import com.example.habitchain.databinding.ItemHabitCategoryBinding

class HabitCategoryAdapter(private val onCategoryClicked: (HabitCategory) -> Unit) :
    ListAdapter<HabitCategory, HabitCategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemHabitCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemHabitCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: HabitCategory) {
            binding.textViewCategoryName.text = category.name
            binding.textViewCategoryIcon.text = category.icon
            binding.root.setOnClickListener { onCategoryClicked(category) }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<HabitCategory>() {
        override fun areItemsTheSame(oldItem: HabitCategory, newItem: HabitCategory): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: HabitCategory, newItem: HabitCategory): Boolean {
            return oldItem == newItem
        }
    }
}