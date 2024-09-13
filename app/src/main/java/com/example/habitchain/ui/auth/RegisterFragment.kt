package com.example.habitchain.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitchain.databinding.FragmentRegisterBinding
import com.example.habitchain.utils.Constants.ERROR_INVALID_EMAIL
import com.example.habitchain.utils.Constants.ERROR_PASSWORDS_MISMATCH
import com.example.habitchain.utils.Constants.ERROR_PASSWORD_LENGTH
import com.example.habitchain.utils.Constants.SUCCESS_REGISTRATION
import com.google.android.material.snackbar.Snackbar
import com.example.habitchain.ui.auth.RegistrationState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeRegistrationState()
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (validateInput(email, password, confirmPassword)) {
                viewModel.register(email, password)
            }
        }

        binding.textViewLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeRegistrationState() {
        viewModel.registrationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegistrationState.Success -> {
                    showMessage(SUCCESS_REGISTRATION)
                    findNavController().navigateUp()
                }

                is RegistrationState.Error -> {
                    showErrorMessage(state.message)
                }

                RegistrationState.Loading -> {
                }
            }
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorMessage(ERROR_INVALID_EMAIL)
            return false
        }
        if (password.length < 6) {
            showErrorMessage(ERROR_PASSWORD_LENGTH)
            return false
        }
        if (password != confirmPassword) {
            showErrorMessage(ERROR_PASSWORDS_MISMATCH)
            return false
        }
        return true
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}