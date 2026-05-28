package com.tkachukmo.bandresearchapp.feature.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.feature.auth.viewmodel.AuthViewModel
import com.tkachukmo.bandresearchapp.feature.auth.viewmodel.OnboardingProfile

private val onboardingGenres = listOf(
    "Rock", "Pop", "Indie", "Metal", "Punk", "Jazz", "Hip-Hop", "Electronic", "Folk", "Classical"
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var step by remember { mutableIntStateOf(0) }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val selectedGenres = remember { mutableStateListOf<String>() }
    var instrument by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var youtubeLink by remember { mutableStateOf("") }
    var audioLink by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val totalSteps = 6
    val passwordProblem = passwordProblem(password, confirmPassword)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun nextStep() {
        localError = null
        when (step) {
            0 -> step = 1
            1 -> if (displayName.isBlank()) localError = "Вкажіть ім'я або сценічний нік" else step = 2
            2 -> if (selectedGenres.isEmpty()) localError = "Оберіть хоча б один стиль" else step = 3
            3 -> if (instrument.isBlank()) localError = "Вкажіть інструмент або роль" else step = 4
            4 -> step = 5
            5 -> {
                when {
                    email.isBlank() || !email.contains("@") -> localError = "Введіть коректну пошту"
                    passwordProblem != null -> localError = passwordProblem
                    else -> {
                        focusManager.clearFocus()
                        viewModel.signUp(
                            email = email.trim(),
                            password = password,
                            profile = OnboardingProfile(
                                displayName = displayName,
                                bio = bio,
                                musicGenres = selectedGenres.toList(),
                                instrument = instrument,
                                experience = experience,
                                location = location,
                                youtubeLink = youtubeLink,
                                audioLink = audioLink
                            ),
                            onSuccess = onRegisterSuccess
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = AuthBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AuthScene {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (step == 0) onNavigateBack() else step -= 1
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AuthText)
                    }
                    Spacer(Modifier.weight(1f))
                    StepDots(current = step, total = totalSteps)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.height(48.dp))
                }

                Spacer(Modifier.height(16.dp))
                BandMatchHeader(subtitle = "CONNECT. CREATE. CONQUER.", compact = true)
                Spacer(Modifier.height(26.dp))

                AuthCard {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (fadeIn(tween(180)) + slideInHorizontally { it / 3 })
                                .togetherWith(fadeOut(tween(120)) + slideOutHorizontally { -it / 3 })
                        },
                        label = "onboardingStep"
                    ) { currentStep ->
                        Column {
                            when (currentStep) {
                                0 -> WelcomeStep()
                                1 -> NameStep(displayName, { displayName = it }, bio, { bio = it })
                                2 -> GenreStep(selectedGenres)
                                3 -> MusicRoleStep(
                                    instrument = instrument,
                                    onInstrumentChange = { instrument = it },
                                    experience = experience,
                                    onExperienceChange = { experience = it },
                                    location = location,
                                    onLocationChange = { location = it }
                                )
                                4 -> LinksStep(
                                    youtubeLink = youtubeLink,
                                    onYoutubeChange = { youtubeLink = it },
                                    audioLink = audioLink,
                                    onAudioChange = { audioLink = it }
                                )
                                5 -> AccountStep(
                                    email = email,
                                    onEmailChange = { email = it },
                                    password = password,
                                    onPasswordChange = { password = it },
                                    confirmPassword = confirmPassword,
                                    onConfirmChange = { confirmPassword = it },
                                    passwordVisible = passwordVisible,
                                    onPasswordVisible = { passwordVisible = !passwordVisible },
                                    confirmVisible = confirmVisible,
                                    onConfirmVisible = { confirmVisible = !confirmVisible }
                                )
                            }
                        }
                    }

                    localError?.let {
                        Spacer(Modifier.height(14.dp))
                        Text(it, color = Color(0xFFFF8FA5), fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                    AuthPrimaryButton(
                        text = if (step == totalSteps - 1) "Створити акаунт" else "Продовжити",
                        onClick = { nextStep() },
                        enabled = true,
                        loading = uiState.isLoading
                    )

                    if (step == 0) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Вже є акаунт? Увійти", color = AuthPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EqualizerLogo(Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(22.dp))
        Text(
            "Вітаємо у BandMatch",
            color = AuthText,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Налаштуємо профіль за кілька швидких кроків, щоб музиканти й гурти бачили, хто ви і що шукаєте.",
            color = AuthMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NameStep(
    displayName: String,
    onNameChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit
) {
    StepTitle("Як вас представити?", "Це ім'я бачитимуть інші користувачі.")
    AuthTextField(displayName, onNameChange, "Ім'я або сценічний нік", icon = Icons.Default.Person)
    Spacer(Modifier.height(16.dp))
    AuthTextField(
        value = bio,
        onValueChange = onBioChange,
        label = "Коротко про себе",
        singleLine = false,
        minLines = 3,
        supportingText = "Наприклад: вокаліст, пишу тексти, шукаю indie-гурт."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreStep(selectedGenres: MutableList<String>) {
    StepTitle("Які стилі вам близькі?", "Оберіть усе, що реально відгукується.")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        onboardingGenres.forEach { genre ->
            val selected = selectedGenres.contains(genre)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) AuthPrimary else AuthInput)
                    .border(1.dp, if (selected) AuthPrimary else AuthStroke, RoundedCornerShape(18.dp))
                    .clickable {
                        if (selected) selectedGenres.remove(genre) else selectedGenres.add(genre)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    genre,
                    color = if (selected) Color(0xFF35125A) else AuthText,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MusicRoleStep(
    instrument: String,
    onInstrumentChange: (String) -> Unit,
    experience: String,
    onExperienceChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit
) {
    StepTitle("Ваша музична роль", "Допоможе точніше підбирати гурти й вакансії.")
    AuthTextField(instrument, onInstrumentChange, "Інструмент або роль", icon = Icons.Default.MusicNote)
    Spacer(Modifier.height(16.dp))
    AuthTextField(experience, onExperienceChange, "Досвід", supportingText = "Наприклад: 2 роки, студійний досвід, початківець.")
    Spacer(Modifier.height(16.dp))
    AuthTextField(location, onLocationChange, "Місто", icon = Icons.Default.Place)
}

@Composable
private fun LinksStep(
    youtubeLink: String,
    onYoutubeChange: (String) -> Unit,
    audioLink: String,
    onAudioChange: (String) -> Unit
) {
    StepTitle("Додайте демо", "Цей крок можна пропустити, але посилання сильно оживляють профіль.")
    AuthTextField(youtubeLink, onYoutubeChange, "YouTube або відео", icon = Icons.Default.Link)
    Spacer(Modifier.height(16.dp))
    AuthTextField(audioLink, onAudioChange, "Аудіо / портфоліо / SoundCloud", icon = Icons.Default.Link)
}

@Composable
private fun AccountStep(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisible: () -> Unit,
    confirmVisible: Boolean,
    onConfirmVisible: () -> Unit
) {
    StepTitle("Останній крок", "Пошта та пароль для входу. Без Google, Apple чи інших сервісів.")
    AuthTextField(email, { onEmailChange(it.trim()) }, "Email", icon = Icons.Default.Email)
    Spacer(Modifier.height(16.dp))
    AuthTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Пароль",
        icon = Icons.Default.Lock,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { PasswordVisibilityButton(passwordVisible, onPasswordVisible) }
    )
    Spacer(Modifier.height(16.dp))
    AuthTextField(
        value = confirmPassword,
        onValueChange = onConfirmChange,
        label = "Повторіть пароль",
        icon = Icons.Default.Lock,
        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { PasswordVisibilityButton(confirmVisible, onConfirmVisible) }
    )
    Spacer(Modifier.height(14.dp))
    PasswordRules(password = password, confirmPassword = confirmPassword)
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Text(title, color = AuthText, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    Text(subtitle, color = AuthMuted, fontSize = 14.sp, lineHeight = 20.sp)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun PasswordRules(password: String, confirmPassword: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PasswordRule("мінімум 8 символів", password.length >= 8)
        PasswordRule("хоча б 1 цифра", password.any { it.isDigit() })
        PasswordRule("хоча б 1 спецсимвол", password.any { !it.isLetterOrDigit() })
        PasswordRule("паролі збігаються", password.isNotBlank() && password == confirmPassword)
    }
}

@Composable
private fun PasswordRule(text: String, passed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(if (passed) AuthPrimary else AuthInput)
                .border(1.dp, if (passed) AuthPrimary else AuthStroke, RoundedCornerShape(99.dp))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                null,
                tint = if (passed) Color(0xFF35125A) else AuthMuted,
                modifier = Modifier.height(14.dp)
            )
        }
        Text(
            text = text,
            color = if (passed) AuthText else AuthMuted,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun passwordProblem(password: String, confirmPassword: String): String? {
    return when {
        password.length < 8 -> "Пароль має містити мінімум 8 символів"
        password.none { it.isDigit() } -> "Пароль має містити хоча б 1 цифру"
        password.none { !it.isLetterOrDigit() } -> "Пароль має містити хоча б 1 спецсимвол"
        password != confirmPassword -> "Паролі не збігаються"
        else -> null
    }
}
