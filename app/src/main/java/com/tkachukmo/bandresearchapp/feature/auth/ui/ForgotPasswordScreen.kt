package com.tkachukmo.bandresearchapp.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.feature.auth.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AuthText)
                    }
                }

                Spacer(Modifier.height(12.dp))
                BandMatchHeader(subtitle = "Відновлення доступу")
                Spacer(Modifier.height(34.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    AuthCard {
                        if (emailSent) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                null,
                                tint = AuthPrimary,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .height(58.dp)
                            )
                            Spacer(Modifier.height(18.dp))
                            Text(
                                "Лист надіслано",
                                color = AuthText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Перевірте пошту $email і відкрийте посилання для встановлення нового паролю.",
                                color = AuthMuted,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(24.dp))
                            AuthPrimaryButton(
                                text = "Повернутися до входу",
                                onClick = onNavigateBack,
                                enabled = true
                            )
                        } else {
                            Text(
                                "Встановити новий пароль",
                                color = AuthText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Введіть email акаунта. Ми надішлемо посилання для зміни паролю.",
                                color = AuthMuted,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            AuthTextField(
                                value = email,
                                onValueChange = { email = it.trim() },
                                label = "Email",
                                icon = Icons.Default.Email,
                                enabled = !uiState.isLoading
                            )
                            Spacer(Modifier.height(24.dp))
                            AuthPrimaryButton(
                                text = "Надіслати посилання",
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.resetPassword(email) {
                                        emailSent = true
                                    }
                                },
                                enabled = email.isNotBlank() && email.contains("@"),
                                loading = uiState.isLoading
                            )
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
