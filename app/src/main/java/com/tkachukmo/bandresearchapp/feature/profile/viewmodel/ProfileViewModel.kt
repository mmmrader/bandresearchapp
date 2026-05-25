package com.tkachukmo.bandresearchapp.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth // Оновлений імпорт
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _userEmail = MutableStateFlow<String?>("Завантаження...")
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // Отримуємо поточну сесію користувача з Supabase Auth
                val currentUser = supabaseClient.auth.currentUserOrNull()
                _userEmail.value = currentUser?.email ?: "Невідомий користувач"
            } catch (e: Exception) {
                _userEmail.value = "Помилка завантаження"
                e.printStackTrace()
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}