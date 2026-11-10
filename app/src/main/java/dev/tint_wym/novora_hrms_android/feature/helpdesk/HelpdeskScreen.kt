package dev.tint_wym.novora_hrms_android.feature.helpdesk

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.HelpdeskTicket
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PrimaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.ScreenHeader
import dev.tint_wym.novora_hrms_android.core.ui.components.SecondaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusPill
import dev.tint_wym.novora_hrms_android.core.ui.components.statusKindFor
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HelpdeskScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<HelpdeskTicket>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showCreate by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = items.isEmpty()
            error = null
            try {
                items = withContext(Dispatchers.IO) { repo.fetchHelpdeskTickets() }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Could not load tickets")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        containerColor = NovoraSurface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                containerColor = NovoraBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New ticket") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (onBack != null) {
                    TextButton(onClick = onBack) { Text("← Back") }
                }
                ScreenHeader(
                    title = "Helpdesk",
                    subtitle = "Support tickets from your account.",
                )
            }
            DisplayText.orEmpty(message).takeIf { it.isNotBlank() }?.let {
                item { SoftBanner(text = it, kind = StatusKind.Success) }
            }
            DisplayText.orEmpty(error).takeIf { it.isNotBlank() }?.let {
                item { SoftBanner(text = it, kind = StatusKind.Danger) }
            }
            if (!loading && items.isEmpty() && error == null) {
                item {
                    EmptyStateCard(
                        title = "No tickets",
                        subtitle = "Open a ticket when you need HR or IT support.",
                    )
                }
            }
            items(items, key = { it.id }) { ticket ->
                PolishCard(contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = DisplayText.orDash(ticket.subject),
                            fontWeight = FontWeight.Bold,
                            color = NovoraInk,
                            modifier = Modifier.weight(1f),
                        )
                        StatusPill(
                            label = DisplayText.orDash(ticket.status),
                            kind = statusKindFor(ticket.status),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = listOf(ticket.category, ticket.priority)
                            .map { DisplayText.orEmpty(it) }
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                            .ifBlank { "General" },
                        style = MaterialTheme.typography.bodySmall,
                        color = NovoraSlate,
                    )
                    if (DisplayText.orEmpty(ticket.description).isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovoraInk,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showCreate) {
        CreateTicketDialog(
            onDismiss = { showCreate = false },
            onSubmit = { subject, description, category, priority ->
                scope.launch {
                    try {
                        val created = withContext(Dispatchers.IO) {
                            repo.createHelpdeskTicket(subject, description, category, priority)
                        }
                        items = listOf(created) + items
                        message = "Ticket opened"
                        showCreate = false
                    } catch (e: Exception) {
                        error = DisplayText.orFallback(e.message, "Could not open ticket")
                    }
                }
            },
        )
    }
}

@Composable
private fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var priority by remember { mutableStateOf("normal") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovoraBorder),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Open ticket",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                PrimaryActionButton(
                    text = "Submit",
                    onClick = {
                        onSubmit(subject.trim(), description.trim(), category.trim(), priority.trim())
                    },
                    enabled = subject.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryActionButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
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
