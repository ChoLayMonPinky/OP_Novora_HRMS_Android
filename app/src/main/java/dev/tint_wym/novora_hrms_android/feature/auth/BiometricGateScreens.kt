package dev.tint_wym.novora_hrms_android.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import dev.tint_wym.novora_hrms_android.core.security.BiometricGate
import dev.tint_wym.novora_hrms_android.core.ui.brand.BrandLockup

@Composable
fun BiometricEnrollScreen(
    userId: String,
    displayName: String,
    onEnrolled: () -> Unit,
    onUnavailableContinue: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val (available, availabilityMessage) = remember {
        BiometricGate.availability(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandLockup(logoSize = 44.dp, subtitle = "HRMS")
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 28.dp)
                .size(64.dp),
        )
        Text(
            text = "Register biometrics",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Hi $displayName. Register fingerprint or face once to unlock Novora next time — like setting up a passkey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        if (error != null) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Button(
            onClick = {
                if (!available || activity == null) {
                    onUnavailableContinue()
                    return@Button
                }
                busy = true
                error = null
                BiometricGate.authenticate(
                    activity = activity,
                    title = "Register biometrics",
                    subtitle = "Confirm fingerprint or face to unlock Novora",
                    onSuccess = {
                        BiometricGate.markEnrolled(context, userId)
                        busy = false
                        onEnrolled()
                    },
                    onError = {
                        busy = false
                        error = it
                    },
                    onCancel = { busy = false },
                )
            },
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (available) "Register fingerprint / face" else "Continue without biometrics")
            }
        }
        if (available) {
            Text(
                text = availabilityMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else if (error == null) {
            Text(
                text = availabilityMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
fun BiometricLockScreen(
    displayName: String,
    onUnlocked: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun unlock() {
        if (activity == null) {
            error = "Biometric prompt is unavailable."
            return
        }
        busy = true
        error = null
        BiometricGate.authenticate(
            activity = activity,
            title = "Unlock Novora",
            subtitle = "Use fingerprint or face to continue",
            onSuccess = {
                busy = false
                onUnlocked()
            },
            onError = {
                busy = false
                error = it
            },
            onCancel = { busy = false },
        )
    }

    LaunchedEffect(Unit) { unlock() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandLockup(logoSize = 44.dp, subtitle = "HRMS")
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 28.dp)
                .size(48.dp),
        )
        Text(
            text = "Unlock Novora",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Welcome back, $displayName. Use fingerprint or face to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        if (error != null) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Button(
            onClick = { unlock() },
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Unlock with biometrics")
            }
        }
    }
}
