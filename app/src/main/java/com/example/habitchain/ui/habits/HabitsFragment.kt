package com.example.habitchain.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentHabitsBinding
import com.example.habitchain.data.model.HabitCategory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitsFragment : Fragment() {

    private lateinit var binding: FragmentHabitsBinding

    private val viewModel: HabitsViewModel by viewModels()
    private lateinit var categoryAdapter: HabitCategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        categoryAdapter = HabitCategoryAdapter { category ->
            val action = HabitsFragmentDirections.actionHabitsFragmentToAddEditHabitFragment(category = category.name)
            findNavController().navigate(action)
        }
        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }
}