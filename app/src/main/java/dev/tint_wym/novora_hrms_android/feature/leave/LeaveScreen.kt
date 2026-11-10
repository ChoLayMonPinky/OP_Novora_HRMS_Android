package dev.tint_wym.novora_hrms_android.feature.leave

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.LeaveBalance
import dev.tint_wym.novora_hrms_android.core.model.LeaveItem
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PrimaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.ScreenHeader
import dev.tint_wym.novora_hrms_android.core.ui.components.SecondaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.components.SectionLabel
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusPill
import dev.tint_wym.novora_hrms_android.core.ui.components.statusKindFor
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccess
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraWarn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private val statusFilters = listOf("ALL", "PENDING", "APPROVED", "REJECTED", "CANCELLED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen() {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<LeaveItem>>(emptyList()) }
    var balances by remember { mutableStateOf<List<LeaveBalance>>(emptyList()) }
    var leaveTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf("ALL") }
    var showApply by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    fun refresh() {
        scope.launch {
            error = null
            loading = items.isEmpty()
            try {
                val result = withContext(Dispatchers.IO) {
                    coroutineScope {
                        val leaveD = async { repo.fetchMyLeave() }
                        val balD = async {
                            try {
                                repo.fetchLeaveBalances()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                        val typesD = async { repo.fetchLeaveTypes() }
                        Triple(leaveD.await(), balD.await(), typesD.await())
                    }
                }
                items = result.first
                balances = result.second
                leaveTypes = result.third
                if (leaveTypes.isEmpty()) {
                    error = "No leave types returned from the server. Ask HR to configure leave types."
                }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Could not load leave")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val filtered = remember(items, statusFilter) {
        if (statusFilter == "ALL") items
        else items.filter { it.status.equals(statusFilter, ignoreCase = true) }
    }

    Scaffold(
        containerColor = NovoraSurface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (leaveTypes.isNotEmpty()) showApply = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
                containerColor = NovoraBlue,
                contentColor = Color.White,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ScreenHeader(
                    title = "Leave",
                    subtitle = "Track balances, apply time off, and follow approval status.",
                )
            }

            if (balances.isNotEmpty()) {
                item { LeaveBalancesCard(balances = balances, onApply = { showApply = true }) }
            }

            DisplayText.orEmpty(message).takeIf { it.isNotBlank() }?.let {
                item { SoftBanner(text = it, kind = StatusKind.Success) }
            }
            DisplayText.orEmpty(error).takeIf { it.isNotBlank() }?.let {
                item { SoftBanner(text = it, kind = StatusKind.Danger) }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    statusFilters.forEach { filter ->
                        val selected = statusFilter == filter
                        FilterPill(
                            label = filter,
                            selected = selected,
                            onClick = { statusFilter = filter },
                        )
                    }
                }
            }

            item { SectionLabel("My requests") }

            if (!loading && filtered.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = if (items.isEmpty()) "No leave requests yet" else "No matches",
                        subtitle = if (items.isEmpty()) {
                            "Tap Apply Leave to submit your first request."
                        } else {
                            "Try another status filter."
                        },
                    )
                }
            }

            items(filtered, key = { it.id }) { leave ->
                PolishCard(contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = DisplayText.orFallback(leave.leaveType, "Leave"),
                            fontWeight = FontWeight.SemiBold,
                            color = NovoraInk,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        StatusPill(
                            label = DisplayText.orDash(leave.status),
                            kind = statusKindFor(leave.status),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = DisplayText.orDash(leave.dateRange),
                        style = MaterialTheme.typography.bodySmall,
                        color = NovoraSlate,
                    )
                    val reasonText = DisplayText.orEmpty(leave.reason)
                    if (reasonText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = reasonText, style = MaterialTheme.typography.bodySmall, color = NovoraInk)
                    }
                    if (leave.status.equals("PENDING", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SecondaryActionButton(
                            text = "Cancel request",
                            onClick = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { repo.cancelLeave(leave.id) }
                                        message = "Cancelled"
                                        items = items.filterNot { it.id == leave.id }
                                        balances = withContext(Dispatchers.IO) {
                                            try {
                                                repo.fetchLeaveBalances()
                                            } catch (_: Exception) {
                                                balances
                                            }
                                        }
                                    } catch (e: Exception) {
                                        error = DisplayText.orFallback(e.message, "Could not cancel")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showApply && leaveTypes.isNotEmpty()) {
        ApplyLeaveDialog(
            leaveTypes = leaveTypes,
            balances = balances,
            onDismiss = { showApply = false },
            onSubmit = { type, start, end, reason ->
                scope.launch {
                    try {
                        val created = withContext(Dispatchers.IO) {
                            repo.createLeave(type, start, end, reason)
                        }
                        message = "Leave submitted"
                        items = listOf(created) + items.filterNot { it.id == created.id }
                        balances = withContext(Dispatchers.IO) {
                            try {
                                repo.fetchLeaveBalances()
                            } catch (_: Exception) {
                                balances
                            }
                        }
                        showApply = false
                    } catch (e: Exception) {
                        error = DisplayText.orFallback(e.message, "Could not submit leave")
                    }
                }
            },
        )
    }
}

@Composable
private fun LeaveBalancesCard(
    balances: List<LeaveBalance>,
    onApply: () -> Unit,
) {
    PolishCard(radius = 20.dp, contentPadding = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Leave balances",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                Text(
                    text = "Year ${balances.firstOrNull()?.balanceYear ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovoraSlate,
                )
            }
            TextButton(onClick = onApply) {
                Text("Apply", color = NovoraBlue, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        balances.take(6).forEach { balance ->
            val total = balance.totalDays.coerceAtLeast(0.1)
            val usedRatio = ((balance.usedDays + balance.pendingDays) / total).toFloat().coerceIn(0f, 1f)
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = DisplayText.orDash(balance.leaveType),
                        fontWeight = FontWeight.SemiBold,
                        color = NovoraInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${fmtDays(balance.remainingDays)} left",
                        color = NovoraBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NovoraBlue,
                    trackColor = NovoraPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Used ${fmtDays(balance.usedDays)} · Pending ${fmtDays(balance.pendingDays)} · Total ${fmtDays(balance.totalDays)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NovoraSlate,
                )
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) NovoraBlue else NovoraPrimaryContainer)
            .border(1.dp, if (selected) NovoraBlue else Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        color = if (selected) Color.White else NovoraBlue,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyLeaveDialog(
    leaveTypes: List<String>,
    balances: List<LeaveBalance>,
    onDismiss: () -> Unit,
    onSubmit: (type: String, start: String, end: String, reason: String) -> Unit,
) {
    var leaveType by remember { mutableStateOf(leaveTypes.firstOrNull().orEmpty()) }
    var startDate by remember { mutableStateOf(WorkRepository.todayIso()) }
    var endDate by remember { mutableStateOf(WorkRepository.todayIso()) }
    var reason by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }
    val remaining = balances.firstOrNull {
        it.leaveType.equals(leaveType, ignoreCase = true)
    }?.remainingDays
    val daySpan = remember(startDate, endDate) {
        val start = parseIsoDate(startDate)
        val end = parseIsoDate(endDate)
        if (start != null && end != null && !end.isBefore(start)) {
            ChronoUnit.DAYS.between(start, end) + 1
        } else {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Apply leave",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                if (remaining != null) {
                    SoftBanner(
                        text = "${DisplayText.orDash(leaveType)} · ${fmtDays(remaining)} days remaining",
                        kind = if (remaining > 0) StatusKind.Info else StatusKind.Warning,
                    )
                }
                Text("Leave type", style = MaterialTheme.typography.labelMedium, color = NovoraSlate)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    leaveTypes.forEach { type ->
                        FilterPill(
                            label = type,
                            selected = leaveType == type,
                            onClick = { leaveType = type },
                        )
                    }
                }
                IsoDatePickerField(
                    label = "Start date",
                    value = startDate,
                    onDateSelected = { picked ->
                        startDate = picked
                        dateError = null
                        val end = parseIsoDate(endDate)
                        val start = parseIsoDate(picked)
                        if (start != null && end != null && end.isBefore(start)) {
                            endDate = picked
                        }
                    },
                )
                IsoDatePickerField(
                    label = "End date",
                    value = endDate,
                    onDateSelected = { picked ->
                        endDate = picked
                        dateError = null
                    },
                )
                if (daySpan != null) {
                    Text(
                        text = if (daySpan == 1L) "1 day selected" else "$daySpan days selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovoraSlate,
                    )
                }
                if (!dateError.isNullOrBlank()) {
                    SoftBanner(text = dateError.orEmpty(), kind = StatusKind.Warning)
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("Optional notes for your manager") },
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryActionButton(
                    text = "Submit request",
                    onClick = {
                        val start = parseIsoDate(startDate)
                        val end = parseIsoDate(endDate)
                        when {
                            start == null || end == null ->
                                dateError = "Pick valid start and end dates"
                            end.isBefore(start) ->
                                dateError = "End date must be on or after start date"
                            else -> {
                                dateError = null
                                onSubmit(leaveType, startDate, endDate, reason.trim())
                            }
                        }
                    },
                    enabled = leaveType.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IsoDatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val initialMillis = remember(value) { isoToUtcMillis(value) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = NovoraBlue)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = NovoraBorder,
                disabledLabelColor = NovoraSlate,
                disabledTextColor = NovoraInk,
                disabledTrailingIconColor = NovoraBlue,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true },
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(utcMillisToIso(millis))
                        }
                        showPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun parseIsoDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: DateTimeParseException) {
        null
    }

private fun isoToUtcMillis(iso: String): Long? =
    parseIsoDate(iso)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

private fun utcMillisToIso(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovoraBlue,
    unfocusedBorderColor = NovoraBorder,
    focusedLabelColor = NovoraBlue,
    cursorColor = NovoraBlue,
)

private fun fmtDays(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
}
