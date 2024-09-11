package com.example.habitchain.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.User
import com.example.habitchain.data.repository.UserRepository
import com.example.habitchain.utils.Constants.ERROR_LOGIN_FAILED
import com.example.habitchain.utils.Constants.LOG_ATTEMPTING_SIGN_IN
import com.example.habitchain.utils.Constants.LOG_SIGN_IN_SUCCESSFUL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthState>()
    val authenticationState: LiveData<AuthState> = _authenticationState

    init {
        _authenticationState.value = AuthState.Unauthenticated
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading
            try {
                //Log.d("LoginViewModel", LOG_ATTEMPTING_SIGN_IN)
                val user = userRepository.signIn(email, password)
                //Log.d("LoginViewModel", LOG_SIGN_IN_SUCCESSFUL)
                _authenticationState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                //Log.e("LoginViewModel", ERROR_LOGIN_FAILED, e)
                _authenticationState.value = AuthState.Error(e.message ?: ERROR_LOGIN_FAILED)
            }
        }
    }

    sealed class AuthState {
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}