package dev.tint_wym.novora_hrms_android.feature.approvals

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.ClaimItem
import dev.tint_wym.novora_hrms_android.core.model.LeaveItem
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.InitialsAvatar
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PrimaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.ScreenHeader
import dev.tint_wym.novora_hrms_android.core.ui.components.SecondaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.SectionLabel
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusPill
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccess
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ApprovalsScreen(user: SessionUser) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var leaveItems by remember { mutableStateOf<List<LeaveItem>>(emptyList()) }
    var claimItems by remember { mutableStateOf<List<ClaimItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var rejectTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // kind to id

    fun refresh() {
        scope.launch {
            error = null
            try {
                val result = withContext(Dispatchers.IO) {
                    coroutineScope {
                        val leaves = async {
                            try {
                                repo.fetchPendingLeave()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                        val claims = async {
                            try {
                                repo.fetchPendingClaims()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                        leaves.await() to claims.await()
                    }
                }
                leaveItems = result.first
                claimItems = result.second
                if (result.first.isEmpty() && result.second.isEmpty()) {
                    // keep empty; no error
                }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Unable to load approvals")
                leaveItems = emptyList()
                claimItems = emptyList()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (user.canApprove) refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Approvals",
                subtitle = "Pending leave and expense claims from your team.",
            )
        }

        if (!user.canApprove) {
            item {
                EmptyStateCard(
                    title = "Approvals not available",
                    subtitle = "Your role does not include approvals. Managers and HR see pending requests here.",
                )
            }
            return@LazyColumn
        }

        DisplayText.orEmpty(message).takeIf { it.isNotBlank() }?.let {
            item { SoftBanner(text = it, kind = StatusKind.Success) }
        }
        DisplayText.orEmpty(error).takeIf { it.isNotBlank() }?.let {
            item { SoftBanner(text = it, kind = StatusKind.Danger) }
        }

        if (leaveItems.isEmpty() && claimItems.isEmpty() && error == null) {
            item {
                EmptyStateCard(
                    title = "All clear",
                    subtitle = "No pending leave or claim approvals right now.",
                )
            }
        }

        if (leaveItems.isNotEmpty()) {
            item { SectionLabel("Leave") }
        }
        items(leaveItems, key = { "leave-${it.id}" }) { leave ->
            PolishCard(contentPadding = 16.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InitialsAvatar(name = DisplayText.orFallback(leave.employeeName, "Employee"))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DisplayText.orFallback(leave.employeeName, "Employee"),
                            fontWeight = FontWeight.Bold,
                            color = NovoraInk,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = DisplayText.orDash(leave.dateRange),
                            style = MaterialTheme.typography.bodySmall,
                            color = NovoraSlate,
                        )
                    }
                    StatusPill(label = "Pending", kind = StatusKind.Warning)
                }
                Spacer(modifier = Modifier.height(10.dp))
                StatusPill(
                    label = DisplayText.orFallback(leave.leaveType, "Leave"),
                    kind = StatusKind.Info,
                )
                val reason = DisplayText.orEmpty(leave.reason)
                if (reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = reason, style = MaterialTheme.typography.bodySmall, color = NovoraInk)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { repo.decideLeave(leave.id, true) }
                                    message = "Approved leave for ${DisplayText.orFallback(leave.employeeName, "employee")}"
                                    leaveItems = leaveItems.filterNot { it.id == leave.id }
                                } catch (e: Exception) {
                                    error = DisplayText.orFallback(e.message, "Could not approve")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovoraSuccess,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Approve", fontWeight = FontWeight.SemiBold)
                    }
                    SecondaryActionButton(
                        text = "Reject",
                        onClick = { rejectTarget = "leave" to leave.id },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (claimItems.isNotEmpty()) {
            item { SectionLabel("Claims") }
        }
        items(claimItems, key = { "claim-${it.id}" }) { claim ->
            PolishCard(contentPadding = 16.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InitialsAvatar(name = DisplayText.orFallback(claim.employeeName, "Employee"))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DisplayText.orFallback(claim.employeeName, "Employee"),
                            fontWeight = FontWeight.Bold,
                            color = NovoraInk,
                        )
                        Text(
                            text = "${DisplayText.orDash(claim.category)} · ${claim.currency} ${"%.2f".format(claim.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovoraSlate,
                        )
                    }
                    StatusPill(label = "Pending", kind = StatusKind.Warning)
                }
                if (DisplayText.orEmpty(claim.description).isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(claim.description, style = MaterialTheme.typography.bodySmall, color = NovoraInk)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { repo.decideClaim(claim.id, true) }
                                    message = "Approved claim for ${DisplayText.orFallback(claim.employeeName, "employee")}"
                                    claimItems = claimItems.filterNot { it.id == claim.id }
                                } catch (e: Exception) {
                                    error = DisplayText.orFallback(e.message, "Could not approve claim")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovoraSuccess,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Approve", fontWeight = FontWeight.SemiBold)
                    }
                    SecondaryActionButton(
                        text = "Reject",
                        onClick = { rejectTarget = "claim" to claim.id },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    rejectTarget?.let { (kind, id) ->
        RejectNoteDialog(
            onDismiss = { rejectTarget = null },
            onConfirm = { note ->
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            if (kind == "leave") repo.decideLeave(id, false, note)
                            else repo.decideClaim(id, false, note)
                        }
                        if (kind == "leave") {
                            leaveItems = leaveItems.filterNot { it.id == id }
                            message = "Leave rejected"
                        } else {
                            claimItems = claimItems.filterNot { it.id == id }
                            message = "Claim rejected"
                        }
                        rejectTarget = null
                    } catch (e: Exception) {
                        error = DisplayText.orFallback(e.message, "Could not reject")
                    }
                }
            },
        )
    }
}

@Composable
private fun RejectNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var note by remember { mutableStateOf("") }
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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Reject request", fontWeight = FontWeight.Bold, color = NovoraInk, style = MaterialTheme.typography.titleLarge)
                Text("Optional note for the employee.", style = MaterialTheme.typography.bodySmall, color = NovoraSlate)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryActionButton(
                    text = "Confirm reject",
                    onClick = { onConfirm(note.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}
