package dev.tint_wym.novora_hrms_android.feature.claims

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import dev.tint_wym.novora_hrms_android.core.model.ClaimItem
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
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val claimCategories = listOf(
    "Meal allowance",
    "Transport",
    "Hotel / stay",
    "Air ticket",
    "Mileage",
    "Wellness",
)

@Composable
fun ClaimsScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<ClaimItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showCreate by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = items.isEmpty()
            error = null
            try {
                items = withContext(Dispatchers.IO) { repo.fetchMyClaims() }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Could not load claims")
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
                text = { Text("New claim") },
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
                    title = "Claims",
                    subtitle = "Expense claims from your account.",
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
                        title = "No claims yet",
                        subtitle = "Submit a meal, transport, or travel expense claim.",
                    )
                }
            }
            items(items, key = { it.id }) { claim ->
                PolishCard(contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DisplayText.orDash(claim.category),
                                fontWeight = FontWeight.Bold,
                                color = NovoraInk,
                            )
                            Text(
                                text = DisplayText.orDash(claim.claimDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = NovoraSlate,
                            )
                        }
                        StatusPill(
                            label = DisplayText.orDash(claim.status),
                            kind = statusKindFor(claim.status),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${claim.currency} ${"%.2f".format(claim.amount)}",
                        fontWeight = FontWeight.SemiBold,
                        color = NovoraInk,
                    )
                    if (DisplayText.orEmpty(claim.vendor).isNotBlank()) {
                        Text(
                            text = "Vendor · ${claim.vendor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovoraSlate,
                        )
                    }
                    if (DisplayText.orEmpty(claim.description).isNotBlank()) {
                        Text(
                            text = claim.description,
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
        CreateClaimDialog(
            onDismiss = { showCreate = false },
            onSubmit = { category, date, amount, currency, vendor, description ->
                scope.launch {
                    try {
                        val created = withContext(Dispatchers.IO) {
                            repo.createClaim(category, date, amount, currency, vendor, description)
                        }
                        items = listOf(created) + items
                        message = "Claim submitted"
                        showCreate = false
                    } catch (e: Exception) {
                        error = DisplayText.orFallback(e.message, "Could not submit claim")
                    }
                }
            },
        )
    }
}

@Composable
private fun CreateClaimDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, String, String, String) -> Unit,
) {
    var category by remember { mutableStateOf(claimCategories.first()) }
    var claimDate by remember { mutableStateOf(WorkRepository.todayIso()) }
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
                    text = "Submit claim",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                Text("Category", style = MaterialTheme.typography.labelMedium, color = NovoraSlate)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    claimCategories.forEach { cat ->
                        val selected = category == cat
                        Text(
                            text = cat,
                            modifier = Modifier
                                .clickable { category = cat }
                                .background(
                                    if (selected) NovoraBlue else NovoraPrimaryContainer,
                                    RoundedCornerShape(20.dp),
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            color = if (selected) Color.White else NovoraBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                OutlinedTextField(
                    value = claimDate,
                    onValueChange = { claimDate = it },
                    label = { Text("Claim date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency (blank = company default)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Vendor (optional)") },
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
                        val parsed = amount.toDoubleOrNull()
                        if (parsed != null && parsed > 0 && claimDate.isNotBlank()) {
                            onSubmit(
                                category,
                                claimDate.trim(),
                                parsed,
                                currency.trim(),
                                vendor.trim(),
                                description.trim(),
                            )
                        }
                    },
                    enabled = amount.toDoubleOrNull()?.let { it > 0 } == true && claimDate.isNotBlank(),
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
