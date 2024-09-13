package com.example.habitchain.ui.auth

import com.example.habitchain.data.model.User

sealed class RegistrationState {
    object Loading : RegistrationState()
    data class Success(val user: User) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}