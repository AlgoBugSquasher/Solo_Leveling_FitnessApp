package com.exork.app.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    object LaunchLegacySignIn : AuthUiEvent()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val WEB_CLIENT_ID = "669660018714-r3hvjc1kkgpf20495h26kfi81aptdtbv.apps.googleusercontent.com"
    }

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(authListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        authListener?.let { auth.removeAuthStateListener(it) }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uiEvent = kotlinx.coroutines.flow.MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun clearError() { _error.value = null }

    fun triggerGoogleSignIn() {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.LaunchLegacySignIn)
        }
    }

    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    private suspend fun signInWithFirebase(idToken: String) {
        try {
            android.util.Log.d("AuthViewModel", "Authenticating with Firebase")
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            
            // Explicitly switch to Main thread for navigation triggering
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                authResult.user?.let {
                    android.util.Log.d("AuthViewModel", "Google Sign-In Success: ${it.email}")
                    _user.value = it
                }
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            android.util.Log.e("AuthViewModel", "Firebase Auth: Invalid or Stale Credentials", e)
            _error.value = "Sign-in session expired. Please try again."
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Firebase Auth with Google failed", e)
            _error.value = "Firebase Authentication failed: ${e.localizedMessage}"
        }
    }

    fun handleLegacySignInResult(data: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.await()
                val idToken = account.idToken
                if (idToken != null) {
                    signInWithFirebase(idToken)
                } else {
                    _error.value = "Google Sign-In failed: No ID Token received"
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Legacy Sign-In failed", e)
                val message = e.localizedMessage ?: "Unknown Error"
                if (message.contains("12501")) {
                    _error.value = "Sign-in cancelled"
                } else {
                    _error.value = "Google Sign-In failed: $message"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, isSignUp: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = if (isSignUp) {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                } else {
                    auth.signInWithEmailAndPassword(email, pass).await()
                }
                result.user?.let {
                    _user.value = it
                    onComplete()
                }
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                android.util.Log.e("AuthViewModel", "Invalid Credentials", e)
                val providers = try { auth.fetchSignInMethodsForEmail(email).await().signInMethods ?: emptyList() } catch(ex: Exception) { emptyList() }
                if (providers.contains(com.google.firebase.auth.GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) && !providers.contains(com.google.firebase.auth.EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                    _error.value = "Account created with Google. Please use Continue with Google or Reset Password."
                } else {
                    _error.value = "Invalid Email or Password"
                }
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                android.util.Log.e("AuthViewModel", "User Collision", e)
                _error.value = "This account was created using Google Sign-In. Please use the Google button."
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Email Auth failed", e)
                _error.value = e.localizedMessage ?: "Authentication failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            viewModelScope.launch {
                _uiEvent.emit(AuthUiEvent.ShowToast("Please enter a valid email address."))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
                _uiEvent.emit(AuthUiEvent.ShowToast("Password reset link sent to your email!"))
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to send reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSignInSuccess(firebaseUser: FirebaseUser) {
        _user.value = firebaseUser
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    suspend fun reauthenticate(password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val email = user.email ?: return Result.failure(Exception("No email found"))
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        return try {
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        return try {
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
