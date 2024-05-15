package com.example.smartofficeworker.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel() : ViewModel() {
    private val _isUserLoggedIn = mutableStateOf(false)
    val isUserLoggedIn: State<Boolean> = _isUserLoggedIn

    private val _error = mutableStateOf(false)
    val error: State<Boolean> = _isUserLoggedIn

    private val _userId = mutableStateOf("")
    val userId= _userId

    fun loginUser(auth: FirebaseAuth,context: Context, email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val user: FirebaseUser? = auth.currentUser
                        if (user != null) {
                                _isUserLoggedIn.value = true
                                Toast.makeText(context, "Log in successful!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        _error.value=true
                        Toast.makeText(context, "Incorrect Login or Password!", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        }
    }
    fun recoverPassword(auth: FirebaseAuth, email: String, context: Context) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                Log.d("PasswordRecovery", "Password reset email sent to $email")
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to send password reset email: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PasswordRecovery", "Error sending password reset email: ${e.message}")
            }
    }

}