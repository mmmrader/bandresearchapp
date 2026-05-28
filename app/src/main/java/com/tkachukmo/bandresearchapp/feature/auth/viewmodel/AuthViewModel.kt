package com.tkachukmo.bandresearchapp.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

// Повертаємо AuthState, який шукають твої екрани
data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class OnboardingProfile(
    val displayName: String,
    val bio: String,
    val musicGenres: List<String>,
    val instrument: String,
    val experience: String,
    val location: String,
    val youtubeLink: String,
    val audioLink: String
)

@Serializable
private data class ProfileOnboardingInsertDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val bio: String? = null,
    @SerialName("music_genres") val musicGenres: List<String> = emptyList(),
    val instrument: String? = null,
    val experience: String? = null,
    val location: String? = null,
    @SerialName("youtube_link") val youtubeLink: String? = null,
    @SerialName("audio_link") val audioLink: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    // --- СТАНИ ДЛЯ UI АВТОРИЗАЦІЇ (Логін, Реєстрація, Відновлення) ---
    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    // --- СТАНИ ДЛЯ ПРОФІЛЮ ---
    private val _userName = MutableStateFlow("Завантаження...")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userInitials = MutableStateFlow("..")
    val userInitials: StateFlow<String> = _userInitials.asStateFlow()

    private val _userEmail = MutableStateFlow("Завантаження...")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    init {
        loadUserData()
    }

    // --- ОБРОБНИК ПОМИЛОК ІНТЕРНЕТУ ТА АВТОРИЗАЦІЇ ---
    private fun getErrorMessage(e: Exception, defaultMsg: String): String {
        val errorMsg = e.localizedMessage ?: ""
        return if (e is UnknownHostException || e is ConnectException || errorMsg.contains("Unable to resolve host") || errorMsg.contains("Failed to connect")) {
            "Відсутнє підключення до інтернету. Перевірте з'єднання."
        } else if (errorMsg.contains("Invalid login credentials")) {
            "Неправильний email або пароль."
        } else if (errorMsg.contains("User already registered")) {
            "Користувач з таким email вже існує."
        } else {
            "$defaultMsg: $errorMsg"
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val sessionUser = supabaseClient.auth.currentUserOrNull()

                if (sessionUser != null) {
                    _userEmail.value = sessionUser.email ?: "Невідома пошта"

                    val displayName = "Михайло Ткачук" // Тимчасова заглушка до створення таблиці users
                    _userName.value = displayName

                    _userInitials.value = displayName
                        .split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                } else {
                    _userEmail.value = "Гість"
                    _userName.value = "Невідомий Користувач"
                    _userInitials.value = "НК"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userEmail.value = "Помилка завантаження"
            }
        }
    }

    // --- ФУНКЦІЇ АВТОРИЗАЦІЇ ---

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                loadUserData()
                onSuccess()
            } catch (e: Exception) {
                // Використовуємо наш новий обробник
                _uiState.update { it.copy(errorMessage = getErrorMessage(e, "Помилка входу")) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        signUp(email, password, null, onSuccess)
    }

    fun signUp(
        email: String,
        password: String,
        profile: OnboardingProfile? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                profile?.let { saveOnboardingProfile(it) }
                loadUserData()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = getErrorMessage(e, "Помилка реєстрації")) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun saveOnboardingProfile(profile: OnboardingProfile) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val dto = ProfileOnboardingInsertDto(
            id = userId,
            displayName = profile.displayName.trim(),
            bio = profile.bio.trim().ifBlank { null },
            musicGenres = profile.musicGenres,
            instrument = profile.instrument.trim().ifBlank { null },
            experience = profile.experience.trim().ifBlank { null },
            location = profile.location.trim().ifBlank { null },
            youtubeLink = profile.youtubeLink.trim().ifBlank { null },
            audioLink = profile.audioLink.trim().ifBlank { null }
        )

        try {
            supabaseClient.postgrest["profiles"].update(
                {
                    set("display_name", dto.displayName)
                    set("bio", dto.bio)
                    set("music_genres", dto.musicGenres)
                    set("instrument", dto.instrument)
                    set("experience", dto.experience)
                    set("location", dto.location)
                    set("youtube_link", dto.youtubeLink)
                    set("audio_link", dto.audioLink)
                }
            ) {
                filter { eq("id", userId) }
            }
        } catch (_: Exception) {
            supabaseClient.postgrest["profiles"].insert(dto)
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                supabaseClient.auth.signOut()

                _userEmail.value = ""
                _userName.value = ""
                _userInitials.value = ""

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = getErrorMessage(e, "Помилка виходу")) }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                supabaseClient.auth.resetPasswordForEmail(email)
                _uiState.update { it.copy(isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = getErrorMessage(e, "Помилка скидання пароля")) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
