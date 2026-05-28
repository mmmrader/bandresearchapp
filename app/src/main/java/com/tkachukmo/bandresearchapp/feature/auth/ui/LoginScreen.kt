package com.tkachukmo.bandresearchapp.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.feature.auth.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                Spacer(Modifier.height(42.dp))
                BandMatchHeader(subtitle = "З поверненням")
                Spacer(Modifier.height(40.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    AuthCard {
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = "Email",
                            icon = Icons.Default.Email,
                            enabled = !uiState.isLoading,
                            modifier = Modifier,
                        )

                        Spacer(Modifier.height(18.dp))

                        AuthTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Пароль",
                            icon = Icons.Default.Lock,
                            enabled = !uiState.isLoading,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                PasswordVisibilityButton(passwordVisible) {
                                    passwordVisible = !passwordVisible
                                }
                            }
                        )

                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Забули пароль?", color = AuthPrimary)
                        }

                        Spacer(Modifier.height(12.dp))

                        AuthPrimaryButton(
                            text = "Увійти",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signIn(email, password, onLoginSuccess)
                            },
                            enabled = email.isNotBlank() && password.isNotBlank(),
                            loading = uiState.isLoading
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text("Новий користувач? Створити профіль", color = AuthPrimary)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
