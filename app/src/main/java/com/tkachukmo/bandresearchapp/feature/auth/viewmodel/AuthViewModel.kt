package com.tkachukmo.bandresearchapp.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Помилка входу"
                )
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signUp(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Помилка реєстрації"
                )
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Помилка відновлення"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signOut()
            if (result.isSuccess) {
                _uiState.value = AuthUiState()
                onSuccess()
            } else {
                _uiState.value = AuthUiState(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Помилка виходу"
                )
            }
        }
    }
}