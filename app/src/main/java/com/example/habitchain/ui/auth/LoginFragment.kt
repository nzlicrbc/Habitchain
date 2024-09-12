package com.example.habitchain.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.habitchain.R
import com.example.habitchain.databinding.FragmentLoginBinding
import com.example.habitchain.utils.Constants.ERROR_EMPTY_FIELDS
import com.example.habitchain.utils.Constants.ERROR_INVALID_EMAIL
import com.example.habitchain.utils.Constants.ERROR_PASSWORD_LENGTH
import com.google.android.material.snackbar.Snackbar
import com.example.habitchain.ui.auth.AuthState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupClickListeners()
        observeAuthState()
        setupSvgAnimation()
    }

    private fun setupSvgAnimation() {
        val animationView = binding.animationView
        val avd = AnimatedVectorDrawableCompat.create(
            requireContext(),
            R.drawable.habit_tracker_animation
        )
        animationView.setImageDrawable(avd)
        avd?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                animationView.post { avd.start() }
            }
        })
        avd?.start()
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
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeAuthState() {
        viewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                }

                is AuthState.Error -> {
                    showErrorMessage(state.message)
                    showLoading(false)
                }

                AuthState.Loading -> {
                    showLoading(true)
                }

                AuthState.Unauthenticated -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showErrorMessage(ERROR_EMPTY_FIELDS)
            return false
        }
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches().not()) {
            showErrorMessage(ERROR_INVALID_EMAIL)
            return false
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            showErrorMessage(ERROR_PASSWORD_LENGTH)
            return false
        }
        return true
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.buttonLogin.isEnabled = !isLoading
        binding.textViewSignUp.isEnabled = !isLoading
    }
}