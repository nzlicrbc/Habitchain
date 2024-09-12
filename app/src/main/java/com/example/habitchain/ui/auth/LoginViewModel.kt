package com.example.habitchain.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.repository.UserRepository
import com.example.habitchain.utils.Constants.ERROR_LOGIN_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authenticationState: LiveData<AuthState> = _authenticationState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading
            try {
                val user = userRepository.signIn(email, password)
                _authenticationState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _authenticationState.value = AuthState.Error(e.message ?: ERROR_LOGIN_FAILED)
            }
        }
    }
}