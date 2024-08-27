package com.example.habitchain.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.User
import com.example.habitchain.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthState>()
    val authenticationState: LiveData<AuthState> = _authenticationState

    private val _rememberMe = MutableLiveData<Boolean>()
    val rememberMe: LiveData<Boolean> = _rememberMe

    init {
        _authenticationState.value = AuthState.Unauthenticated
        _rememberMe.value = userRepository.isRememberMeEnabled()
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null && _rememberMe.value == true) {
            _authenticationState.value = AuthState.Authenticated(currentUser)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading
            try {
                Log.d(TAG, "Attempting to sign up")
                val user = userRepository.signUp(email, password)
                Log.d(TAG, "Sign up successful")
                _authenticationState.value = AuthState.Authenticated(user)
                setRememberMe(_rememberMe.value ?: false)
            } catch (e: Exception) {
                Log.e(TAG, "Sign up failed", e)
                _authenticationState.value = AuthState.Error(e.message ?: "Kayıt başarısız oldu")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading
            try {
                Log.d(TAG, "Attempting to sign in")
                val user = userRepository.signIn(email, password)
                Log.d(TAG, "Sign in successful")
                _authenticationState.value = AuthState.Authenticated(user)
                setRememberMe(_rememberMe.value ?: false)
            } catch (e: Exception) {
                Log.e(TAG, "Sign in failed", e)
                _authenticationState.value = AuthState.Error(e.message ?: "Giriş başarısız oldu")
            }
        }
    }

    fun setRememberMe(isChecked: Boolean) {
        _rememberMe.value = isChecked
        userRepository.setRememberMe(isChecked)
        Log.d(TAG, "Remember me set to: $isChecked")
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to reset password for email: $email")
                userRepository.resetPassword(email)
                Log.d(TAG, "Password reset email sent successfully")
                _authenticationState.value = AuthState.PasswordResetSent
            } catch (e: Exception) {
                Log.e(TAG, "Password reset failed", e)
                _authenticationState.value = AuthState.Error(e.message ?: "Şifre sıfırlama başarısız oldu")
            }
        }
    }

    sealed class AuthState {
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        object PasswordResetSent : AuthState()
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}