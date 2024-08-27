package com.example.habitchain.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
        observeRememberMe()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (validateInput(email, password)) {
                viewModel.signIn(email, password)
            }
        }

        binding.textViewSignUp.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (validateInput(email, password)) {
                viewModel.signUp(email, password)
            }
        }

        binding.textViewForgotPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (email.isNotBlank()) {
                viewModel.resetPassword(email)
            } else {
                showErrorMessage("Lütfen şifresini sıfırlamak istediğiniz hesabın e-posta adresini girin.")
            }
        }

        binding.checkBoxRememberMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRememberMe(isChecked)
        }

        binding.textViewSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeAuthState() {
        viewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.AuthState.Authenticated -> {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is LoginViewModel.AuthState.Error -> {
                    showErrorMessage(state.message)
                    showLoading(false)
                }
                is LoginViewModel.AuthState.Loading -> {
                    showLoading(true)
                }
                is LoginViewModel.AuthState.Unauthenticated -> {
                    showLoading(false)
                }
                is LoginViewModel.AuthState.PasswordResetSent -> {
                    showMessage("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.")
                }
            }
        }
    }

    private fun observeRememberMe() {
        viewModel.rememberMe.observe(viewLifecycleOwner) { isRemembered ->
            binding.checkBoxRememberMe.isChecked = isRemembered
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showErrorMessage("Email ve şifre boş olamaz")
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
        return true
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.buttonLogin.isEnabled = !isLoading
        binding.textViewSignUp.isEnabled = !isLoading
        binding.textViewForgotPassword.isEnabled = !isLoading
        binding.checkBoxRememberMe.isEnabled = !isLoading
    }
}