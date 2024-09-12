package com.example.habitchain.ui.auth

import com.example.habitchain.data.model.User

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}