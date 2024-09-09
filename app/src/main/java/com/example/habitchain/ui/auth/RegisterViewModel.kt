package com.example.habitchain.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.model.User
import com.example.habitchain.data.repository.UserRepository
import com.example.habitchain.utils.Constants.ERROR_REGISTRATION_FAILED
import com.example.habitchain.utils.Constants.LOG_REGISTER_ATTEMPT
import com.example.habitchain.utils.Constants.LOG_REGISTER_SUCCESS
import com.example.habitchain.utils.Constants.TAG
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
                Log.d(TAG, "$LOG_REGISTER_ATTEMPT$email")
                val user = userRepository.signUp(email, password)
                Log.d(TAG, LOG_REGISTER_SUCCESS)
                _registrationState.value = RegistrationState.Success(user)
            } catch (e: Exception) {
                Log.e(TAG, ERROR_REGISTRATION_FAILED, e)
                _registrationState.value =
                    RegistrationState.Error(e.message ?: ERROR_REGISTRATION_FAILED)
            }
        }
    }

    sealed class RegistrationState {
        object Loading : RegistrationState()
        data class Success(val user: User) : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }
}