package com.shieldai.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldai.app.data.model.LoginRequest
import com.shieldai.app.data.model.RegisterRequest
import com.shieldai.app.data.model.UserProfile
import com.shieldai.app.data.repository.ShieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: UserProfile? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: ShieldRepository
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Email and password are required")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val result = repository.login(LoginRequest(email = email.trim(), password = password))

            uiState = result.fold(
                onSuccess = { auth ->
                    uiState.copy(isLoading = false, isSuccess = true, user = auth.user)
                },
                onFailure = { error ->
                    uiState.copy(isLoading = false, error = error.message ?: "Login failed")
                }
            )
        }
    }

    fun register(fullName: String, email: String, phone: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Name, email, and password are required")
            return
        }
        if (password.length < 8) {
            uiState = uiState.copy(error = "Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val result = repository.register(
                RegisterRequest(
                    fullName = fullName.trim(),
                    email = email.trim(),
                    phoneNumber = phone.ifBlank { null },
                    password = password
                )
            )

            uiState = result.fold(
                onSuccess = { auth ->
                    uiState.copy(isLoading = false, isSuccess = true, user = auth.user)
                },
                onFailure = { error ->
                    uiState.copy(isLoading = false, error = error.message ?: "Registration failed")
                }
            )
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
