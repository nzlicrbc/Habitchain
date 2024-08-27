package com.example.habitchain.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.habitchain.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun signIn(email: String, password: String): User {
        return try {
            Log.d(TAG, "Attempting to sign in with email: $email")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign in successful")
            User(
                id = authResult.user?.uid ?: "",
                email = authResult.user?.email ?: "",
                displayName = authResult.user?.displayName
            )
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network error during sign in", e)
            throw Exception("İnternet bağlantınızı kontrol edin ve tekrar deneyin.")
        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase error during sign in", e)
            throw Exception("Giriş yapılırken bir hata oluştu: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            throw Exception("Beklenmeyen bir hata oluştu: ${e.message}")
        }
    }

    suspend fun signUp(email: String, password: String): User {
        return try {
            Log.d(TAG, "Attempting to sign up with email: $email")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign up successful")
            User(
                id = authResult.user?.uid ?: "",
                email = authResult.user?.email ?: "",
                displayName = authResult.user?.displayName
            )
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network error during sign up", e)
            throw Exception("İnternet bağlantınızı kontrol edin ve tekrar deneyin.")
        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase error during sign up", e)
            throw Exception("Kayıt olurken bir hata oluştu: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign up", e)
            throw Exception("Beklenmeyen bir hata oluştu: ${e.message}")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        Log.d(TAG, "User signed out")
    }

    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        Log.d(TAG, "Current user: ${firebaseUser?.email}")
        return firebaseUser?.let {
            User(
                id = it.uid,
                email = it.email ?: "",
                displayName = it.displayName
            )
        }
    }

    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance()
            Log.d(TAG, "Firebase is initialized")
            true
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Firebase is not initialized", e)
            false
        }
    }

    suspend fun resetPassword(email: String) {
        try {
            Log.d(TAG, "Attempting to send password reset email to: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent successfully")
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network error during password reset", e)
            throw Exception("İnternet bağlantınızı kontrol edin ve tekrar deneyin.")
        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase error during password reset", e)
            throw Exception("Şifre sıfırlama e-postası gönderilirken bir hata oluştu: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during password reset", e)
            throw Exception("Beklenmeyen bir hata oluştu: ${e.message}")
        }
    }

    fun setRememberMe(isRemembered: Boolean) {
        sharedPreferences.edit().putBoolean(REMEMBER_ME_KEY, isRemembered).apply()
        Log.d(TAG, "Remember me set to: $isRemembered")
    }

    fun isRememberMeEnabled(): Boolean {
        val isRemembered = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
        Log.d(TAG, "Remember me is enabled: $isRemembered")
        return isRemembered
    }

    companion object {
        private const val TAG = "UserRepository"
        private const val REMEMBER_ME_KEY = "remember_me"
    }
}