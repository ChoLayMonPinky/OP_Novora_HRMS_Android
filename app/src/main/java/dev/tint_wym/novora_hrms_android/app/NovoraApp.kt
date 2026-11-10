package dev.tint_wym.novora_hrms_android.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tint_wym.novora_hrms_android.core.ui.brand.BrandLockup
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlueDark
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import dev.tint_wym.novora_hrms_android.feature.auth.AuthViewModel
import dev.tint_wym.novora_hrms_android.feature.auth.LoginScreen
import dev.tint_wym.novora_hrms_android.feature.auth.RegisterScreen
import dev.tint_wym.novora_hrms_android.feature.shell.MainShell

private enum class AuthScreen {
    Login,
    Register,
}

@Composable
fun NovoraApp() {
    val authViewModel: AuthViewModel = viewModel()
    val state by authViewModel.state.collectAsState()
    var screen by remember { mutableStateOf(AuthScreen.Login) }

    when {
        state.bootstrapping -> {
            SplashScreen()
        }

        state.user != null -> {
            MainShell(
                user = checkNotNull(state.user),
                onLogout = {
                    screen = AuthScreen.Login
                    authViewModel.logout()
                },
            )
        }

        else -> {
            when (screen) {
                AuthScreen.Login -> {
                    LoginScreen(
                        loading = state.loginLoading,
                        error = state.loginError,
                        onLogin = authViewModel::login,
                        onBackToLanding = null,
                        onGoRegister = { screen = AuthScreen.Register },
                    )
                }

                AuthScreen.Register -> {
                    RegisterScreen(
                        loading = state.registerLoading,
                        error = state.registerError,
                        onRegister = authViewModel::register,
                        onBackToLanding = { screen = AuthScreen.Login },
                        onGoLogin = { screen = AuthScreen.Login },
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        NovoraBlueDark,
                        NovoraBlue,
                        NovoraSurface,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            BrandLockup(
                logoSize = 72.dp,
                subtitle = "HRMS",
                titleStyle = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Loading your workspace…",
                style = MaterialTheme.typography.bodyMedium,
                color = NovoraSlate,
                fontWeight = FontWeight.Medium,
            )
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(28.dp),
                color = NovoraBlue,
                strokeWidth = 3.dp,
            )
        }
    }
}
