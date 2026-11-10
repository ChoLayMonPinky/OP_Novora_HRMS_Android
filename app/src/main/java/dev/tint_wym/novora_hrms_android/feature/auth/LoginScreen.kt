package dev.tint_wym.novora_hrms_android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.brand.BrandLockup
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PrimaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlueDark
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (email: String, password: String) -> Unit,
    onBackToLanding: (() -> Unit)? = null,
    onGoRegister: (() -> Unit)? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(NovoraBlueDark, NovoraBlue, NovoraPrimaryContainer.copy(alpha = 0.35f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            BrandLockup(
                logoSize = 48.dp,
                subtitle = "HRMS",
                titleStyle = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Sign in to manage your work day",
                style = MaterialTheme.typography.bodyMedium,
                color = NovoraSlate,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )

            PolishCard(radius = 20.dp, contentPadding = 20.dp) {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Work email") },
                    placeholder = { Text("you@company.com") },
                    singleLine = true,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onLogin(email, password) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                val errorText = DisplayText.orEmpty(error)
                if (errorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SoftBanner(text = errorText, kind = StatusKind.Danger)
                }

                Spacer(modifier = Modifier.height(20.dp))
                PrimaryActionButton(
                    text = "Sign in",
                    onClick = { onLogin(email, password) },
                    enabled = !loading,
                    loading = loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (loading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = NovoraBlue,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBackToLanding != null) {
                    TextButton(onClick = onBackToLanding) {
                        Text("Back", color = NovoraSlate)
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }
                if (onGoRegister != null) {
                    TextButton(onClick = onGoRegister) {
                        Text("Create account", color = NovoraBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovoraBlue,
    unfocusedBorderColor = NovoraBorder,
    focusedLabelColor = NovoraBlue,
    cursorColor = NovoraBlue,
)
