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
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                Log.d(TAG, "Attempting to register user with email: $email")
                val user = userRepository.signUp(email, password)
                Log.d(TAG, "User registration successful")
                _registrationState.value = RegistrationState.Success(user)
            } catch (e: Exception) {
                Log.e(TAG, "User registration failed", e)
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "Kayıt işlemi sırasında bir hata oluştu")
            }
        }
    }

    sealed class RegistrationState {
        object Loading : RegistrationState()
        data class Success(val user: User) : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}