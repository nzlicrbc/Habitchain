package com.example.habitchain.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.habitchain.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) {

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    suspend fun signUp(email: String, password: String): User {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return User(authResult.user?.uid ?: "", email)
    }

    suspend fun signIn(email: String, password: String): User {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return User(authResult.user?.uid ?: "", email)
    }

    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { User(it.uid, it.email ?: "") }
    }
}