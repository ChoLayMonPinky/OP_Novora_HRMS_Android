package dev.tint_wym.novora_hrms_android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.tint_wym.novora_hrms_android.core.ui.brand.BrandLockup

@Composable
fun LandingScreen(
    onSignIn: () -> Unit,
    onStartTrial: () -> Unit = onSignIn,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 28.dp, vertical = 36.dp),
        ) {
            BrandLockup(
                logoSize = 48.dp,
                subtitle = "HRMS SOFTWARE",
                titleStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 28.dp),
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "All-in-One HR Operating System",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0EA5E9),
                    maxLines = 2,
                )
            }

            Text(
                text = "The Workforce OS for Forward-Thinking Teams.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 16.dp),
            )

            Text(
                text = "Sync biometric attendance to payroll, leave, and performance — built for teams that want administrative speed.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
            )

            Button(
                onClick = onStartTrial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            ) {
                Text("Start Free Trial", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Sign in", fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Why teams choose Novora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
            )

            BenefitCard(
                icon = Icons.Outlined.AccessTime,
                title = "Save HR Admin Time",
                body = "Automate leave, documents, and timesheets — reclaim hours every month.",
            )
            BenefitCard(
                icon = Icons.Outlined.VerifiedUser,
                title = "Reduce Payroll Errors",
                body = "Biometric check-ins feed wage calculations for cleaner disbursements.",
            )
            BenefitCard(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Elevate Staff Experience",
                body = "Employees request leave, clock in, and check pay from their phones.",
            )
            BenefitCard(
                icon = Icons.Outlined.Dashboard,
                title = "Real-Time Auditing",
                body = "Live visibility across leave, headcount, and attendance averages.",
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020617))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Ready to streamline HR & attendance?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Set up your workspace in minutes. No credit card required.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
            )
            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            ) {
                Text("Continue to Sign in", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onSignIn) {
                Text("Already have an account? Sign in", color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun BenefitCard(
    icon: ImageVector,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2563EB).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), style = MaterialTheme.typography.labelLarge)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
