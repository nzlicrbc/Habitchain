package com.example.habitchain.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitchain.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            val username = binding.editTextUsername.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (validateInput(username, email, password, confirmPassword)) {
                viewModel.register(username, email, password)
            }
        }

        binding.textViewLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeRegistrationState() {
        viewModel.registrationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegistrationState.Success -> {
                    showMessage("Kayıt başarılı! Giriş yapabilirsiniz.")
                    findNavController().navigateUp()
                }
                is RegisterViewModel.RegistrationState.Error -> {
                    showErrorMessage(state.message)
                }
                is RegisterViewModel.RegistrationState.Loading -> {
                }
            }
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            showErrorMessage("Tüm alanları doldurun")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorMessage("Geçerli bir email adresi girin")
            return false
        }
        if (password.length < 6) {
            showErrorMessage("Şifre en az 6 karakter olmalıdır")
            return false
        }
        if (password != confirmPassword) {
            showErrorMessage("Şifreler eşleşmiyor")
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