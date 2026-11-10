package dev.tint_wym.novora_hrms_android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.tint_wym.novora_hrms_android.core.ui.brand.BrandLockup
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface

private val passwordRegex =
    Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$")

@Composable
fun RegisterScreen(
    loading: Boolean,
    error: String?,
    onRegister: (email: String, password: String, companyName: String, fullName: String?) -> Unit,
    onBackToLanding: () -> Unit,
    onGoLogin: () -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var companyError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        fullNameError = null
        companyError = null
        emailError = null
        passwordError = null
        confirmError = null
        termsError = null

        var ok = true
        val name = fullName.trim()
        if (name.length > 200) {
            fullNameError = "Name must be at most 200 characters"
            ok = false
        }

        val company = companyName.trim()
        if (company.isEmpty()) {
            companyError = "Company name is required"
            ok = false
        } else if (company.length !in 2..120) {
            companyError = "Company name must be 2–120 characters"
            ok = false
        }

        val mail = email.trim()
        if (mail.isEmpty()) {
            emailError = "Email is required"
            ok = false
        } else if (!mail.contains('@') || !mail.contains('.')) {
            emailError = "Enter a valid email address"
            ok = false
        }

        if (password.isEmpty()) {
            passwordError = "Password is required"
            ok = false
        } else if (!passwordRegex.matches(password)) {
            passwordError =
                "Password must be 8–72 characters and include uppercase, lowercase, a number, and a symbol"
            ok = false
        }

        if (confirmPassword.isEmpty()) {
            confirmError = "Confirm your password"
            ok = false
        } else if (confirmPassword != password) {
            confirmError = "Passwords do not match"
            ok = false
        }

        if (!acceptedTerms) {
            termsError = "Please accept the terms to continue"
            ok = false
        }

        if (!ok) return
        onRegister(mail, password, company, name.ifBlank { null })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BrandLockup(
            logoSize = 40.dp,
            subtitle = "HRMS",
            titleStyle = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Join as an employee. An admin assigns your position after review.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; fullNameError = null },
            label = { Text("Full name") },
            placeholder = { Text("Optional — e.g. Alex Morgan") },
            singleLine = true,
            enabled = !loading,
            isError = fullNameError != null,
            supportingText = fullNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it; companyError = null },
            label = { Text("Company name") },
            placeholder = { Text("Your organization") },
            singleLine = true,
            enabled = !loading,
            isError = companyError != null,
            supportingText = companyError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text("Work email") },
            placeholder = { Text("you@company.com") },
            singleLine = true,
            enabled = !loading,
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Password") },
            placeholder = { Text("8+ chars, upper, lower, number, symbol") },
            singleLine = true,
            enabled = !loading,
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = null },
            label = { Text("Confirm password") },
            placeholder = { Text("Re-enter password") },
            singleLine = true,
            enabled = !loading,
            isError = confirmError != null,
            supportingText = confirmError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = {
                    acceptedTerms = it
                    termsError = null
                },
                enabled = !loading,
            )
            Text(
                text = "I agree to the Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (termsError != null) {
            Text(
                text = termsError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Button(
            onClick = ::submit,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text("Create account", fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onBackToLanding, enabled = !loading) {
                Text("Back to sign in")
            }
            TextButton(onClick = onGoLogin, enabled = !loading) {
                Text("Sign in")
            }
        }
    }
}
