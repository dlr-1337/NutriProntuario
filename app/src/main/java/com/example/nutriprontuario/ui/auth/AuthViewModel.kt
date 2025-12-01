package com.example.nutriprontuario.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    data class AuthState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    private val auth = Firebase.auth

    private val _state = MutableLiveData(AuthState())
    val state: LiveData<AuthState> = _state

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value?.copy(
                error = "Preencha e-mail e senha."
            )
            return
        }
        _state.value = _state.value?.copy(isLoading = true, error = null, success = false)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = AuthState(success = true)
                } else {
                    _state.value = AuthState(
                        error = task.exception.asMessage()
                    )
                }
            }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value?.copy(
                error = "Preencha e-mail e senha."
            )
            return
        }
        _state.value = _state.value?.copy(isLoading = true, error = null, success = false)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = AuthState(success = true)
                } else {
                    _state.value = AuthState(
                        error = task.exception.asMessage()
                    )
                }
            }
    }

    fun clearError() {
        _state.value = _state.value?.copy(error = null)
    }

    private fun Exception?.asMessage(): String {
        return when (this) {
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> "Credenciais inválidas. Verifique e tente novamente."
            else -> this?.localizedMessage ?: "Algo deu errado. Tente novamente."
        }
    }
}
