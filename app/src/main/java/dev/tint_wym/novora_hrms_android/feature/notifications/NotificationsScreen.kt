package dev.tint_wym.novora_hrms_android.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.NotificationItem
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.ScreenHeader
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusPill
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NotificationsScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    fun refresh() {
        scope.launch {
            loading = items.isEmpty()
            error = null
            try {
                items = withContext(Dispatchers.IO) { repo.fetchNotifications() }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Could not load notifications")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScreenHeader(
                    title = "Notifications",
                    subtitle = "Leave decisions, payroll, and company updates.",
                    modifier = Modifier.weight(1f),
                )
                if (onBack != null) {
                    TextButton(onClick = onBack) {
                        Text("Back", color = NovoraBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        DisplayText.orEmpty(error).takeIf { it.isNotBlank() }?.let {
            item {
                SoftBanner(text = it, kind = StatusKind.Danger)
                TextButton(onClick = { refresh() }) { Text("Retry") }
            }
        }

        if (!loading && items.isEmpty() && error == null) {
            item {
                EmptyStateCard(
                    title = "You're all caught up",
                    subtitle = "New leave and payroll alerts will appear here.",
                )
            }
        }

        items(items, key = { it.id }) { note ->
            PolishCard(
                contentPadding = 14.dp,
                modifier = Modifier.clickable {
                    if (!note.read) {
                        scope.launch {
                            try {
                                val updated = withContext(Dispatchers.IO) {
                                    repo.markNotificationRead(note.id)
                                }
                                items = items.map { if (it.id == note.id) updated else it }
                            } catch (_: Exception) {
                                // Keep list usable even if mark-read fails.
                            }
                        }
                    }
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = DisplayText.orDash(note.title),
                        fontWeight = FontWeight.Bold,
                        color = NovoraInk,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StatusPill(
                        label = if (note.read) "Read" else "New",
                        kind = if (note.read) StatusKind.Neutral else StatusKind.Info,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = DisplayText.orDash(note.message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NovoraSlate,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = DisplayText.orEmpty(note.createdAt).ifBlank { DisplayText.orDash(note.type) },
                    style = MaterialTheme.typography.labelSmall,
                    color = NovoraSlate,
                )
            }
        }
    }
}
