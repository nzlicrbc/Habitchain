package com.example.habitchain.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.habitchain.databinding.FragmentHabitProgressBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitProgressFragment : Fragment() {

    private lateinit var binding: FragmentHabitProgressBinding
    private val viewModel: HabitProgressViewModel by viewModels()
    private val args: HabitProgressFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHabitProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadHabit(args.habitId)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.habit.observe(viewLifecycleOwner) { habit ->
            habit?.let {
                binding.textViewHabitName.text = it.name
                binding.textViewGoal.text = "Goal: ${it.goal} ${it.unit}"
                binding.textViewCurrentProgress.text =
                    "Current progress: ${it.currentProgress} ${it.unit}"
                binding.editTextProgress.setText(it.currentProgress.toString())
            }
        }

        viewModel.updateComplete.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Progress updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, "Error updating progress", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.buttonUpdateProgress.setOnClickListener {
            val progress = binding.editTextProgress.text.toString().toIntOrNull()
            if (progress != null) {
                viewModel.updateHabitProgress(progress)
            } else {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}