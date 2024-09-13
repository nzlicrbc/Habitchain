package com.example.habitchain.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitchain.data.repository.UserRepository
import com.example.habitchain.utils.Constants.ERROR_REGISTRATION_FAILED
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
                val user = userRepository.signUp(email, password)
                _registrationState.value = RegistrationState.Success(user)
            } catch (e: Exception) {
                _registrationState.value =
                    RegistrationState.Error(e.message ?: ERROR_REGISTRATION_FAILED)
            }
        }
    }
}