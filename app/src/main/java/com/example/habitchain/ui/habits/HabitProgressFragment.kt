package com.example.habitchain.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.habitchain.databinding.FragmentHabitProgressBinding
import com.example.habitchain.databinding.DialogManualInputBinding
import com.example.habitchain.utils.Constants.CANCEL
import com.example.habitchain.utils.Constants.ENTER_PROGRESS
import com.example.habitchain.utils.Constants.UPDATE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitProgressFragment : Fragment() {

    private var _binding: FragmentHabitProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitProgressViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()

        arguments?.getInt("habitId")?.let { habitId ->
            viewModel.loadHabit(habitId)
        }
    }

    private fun setupObservers() {
        viewModel.habit.observe(viewLifecycleOwner) { habit ->
            binding.textViewHabitName.text = habit.name
            binding.textViewCurrentProgress.text = habit.currentProgress.toString()
            binding.textViewUnit.text = habit.unit
            binding.textViewGoal.text = "/ ${habit.goal} ${habit.unit}"
        }
    }

    private fun setupListeners() {
        binding.buttonIncrease.setOnClickListener {
            viewModel.incrementProgress()
        }
        binding.buttonDecrease.setOnClickListener {
            viewModel.decrementProgress()
        }
        binding.buttonReset.setOnClickListener {
            viewModel.resetProgress()
        }
        binding.buttonManualInput.setOnClickListener {
            showManualInputDialog()
        }
    }

    private fun showManualInputDialog() {
        val dialogBinding = DialogManualInputBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(ENTER_PROGRESS)
            .setView(dialogBinding.root)
            .setPositiveButton(UPDATE) { _, _ ->
                val input = dialogBinding.editTextManualInput.text?.toString() ?: ""
                input.toIntOrNull()?.let { progress ->
                    viewModel.updateProgress(progress)
                }
            }
            .setNegativeButton(CANCEL, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}