package dev.tint_wym.novora_hrms_android.feature.attendance

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.AttendanceItem
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.IconWell
import dev.tint_wym.novora_hrms_android.core.ui.components.MetricCell
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
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AttendanceScreen() {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var logs by remember { mutableStateOf<List<AttendanceItem>>(emptyList()) }
    var statusFilter by remember { mutableStateOf("ALL") }

    val today = remember { WorkRepository.todayIso() }
    val todayLog = logs.firstOrNull { it.workDate == today }
    val punchedIn = todayLog?.checkInTime != null
    val punchedOut = todayLog?.checkOutTime != null
    val statusLabel = when {
        punchedOut -> "Completed"
        punchedIn -> "Checked in"
        else -> "Not punched"
    }
    val statusKind = when {
        punchedOut -> StatusKind.Success
        punchedIn -> StatusKind.Info
        else -> StatusKind.Warning
    }
    val filters = remember(logs) {
        listOf("ALL") + logs.map { it.status.ifBlank { "UNKNOWN" }.uppercase() }.distinct().sorted()
    }
    val filtered = remember(logs, statusFilter) {
        if (statusFilter == "ALL") logs
        else logs.filter { it.status.equals(statusFilter, ignoreCase = true) }
    }

    fun refresh() {
        scope.launch {
            val showSpinner = logs.isEmpty()
            if (showSpinner) loading = true
            error = null
            try {
                logs = withContext(Dispatchers.IO) { repo.fetchMyAttendance() }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Failed to load attendance")
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Attendance",
                subtitle = "Shift clock and recent punch history.",
            )
        }

        item {
            PolishCard(radius = 20.dp, contentPadding = 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        IconWell(icon = Icons.Filled.Schedule)
                        Column {
                            Text(
                                text = "Today's clock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NovoraInk,
                            )
                            Text(text = today, style = MaterialTheme.typography.bodySmall, color = NovoraSlate)
                            Text(
                                text = when {
                                    punchedOut -> "Status · ${DisplayText.orDash(todayLog?.status)}"
                                    punchedIn -> "Checked in · live from attendance API"
                                    else -> "Punch times come from attendance API"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = NovoraSlate,
                            )
                        }
                    }
                    StatusPill(label = statusLabel, kind = statusKind)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "Punch in",
                        value = WorkRepository.formatClock(todayLog?.checkInTime),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCell(
                        label = "Punch out",
                        value = WorkRepository.formatClock(todayLog?.checkOutTime),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCell(
                        label = "Hours",
                        value = todayLog?.workHours?.let { "%.2f".format(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryActionButton(
                        text = "Punch In",
                        enabled = !busy && !punchedIn,
                        loading = busy && !punchedIn,
                        onClick = {
                            scope.launch {
                                busy = true
                                message = null
                                error = null
                                try {
                                    val item = withContext(Dispatchers.IO) { repo.checkIn() }
                                    logs = listOf(item) + logs.filterNot { it.workDate == today || it.id == item.id }
                                    message = "Checked in successfully"
                                } catch (e: Exception) {
                                    error = DisplayText.orFallback(e.message, "Check-in failed")
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryActionButton(
                        text = "Punch Out",
                        enabled = !busy && punchedIn && !punchedOut,
                        onClick = {
                            scope.launch {
                                busy = true
                                message = null
                                error = null
                                try {
                                    val item = withContext(Dispatchers.IO) { repo.checkOut() }
                                    logs = listOf(item) + logs.filterNot { it.workDate == today || it.id == item.id }
                                    message = "Checked out successfully"
                                } catch (e: Exception) {
                                    error = DisplayText.orFallback(e.message, "Check-out failed")
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (loading && logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = NovoraBlue, strokeWidth = 3.dp)
                }
            }
        }

        DisplayText.orEmpty(message).takeIf { it.isNotBlank() }?.let {
            item { SoftBanner(text = it, kind = StatusKind.Success) }
        }
        DisplayText.orEmpty(error).takeIf { it.isNotBlank() }?.let {
            item { SoftBanner(text = it, kind = StatusKind.Danger) }
        }

        item { SectionLabel("Recent logs") }

        if (logs.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    filters.forEach { filter ->
                        val selected = statusFilter == filter
                        val count = if (filter == "ALL") logs.size else logs.count {
                            it.status.equals(filter, ignoreCase = true)
                        }
                        Text(
                            text = "$filter ($count)",
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) NovoraBlue else NovoraPrimaryContainer)
                                .border(
                                    1.dp,
                                    if (selected) NovoraBlue else Color(0xFFC7D2FE),
                                    RoundedCornerShape(20.dp),
                                )
                                .clickable { statusFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            color = if (selected) Color.White else NovoraBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        if (!loading && filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (logs.isEmpty()) "No attendance yet" else "No matches",
                    subtitle = if (logs.isEmpty()) {
                        "Punch in to start logging your work day."
                    } else {
                        "Try another status filter."
                    },
                )
            }
        }

        items(filtered.take(20), key = { it.id.ifBlank { it.workDate } }) { log ->
            PolishCard(contentPadding = 14.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = DisplayText.orDash(log.workDate),
                        fontWeight = FontWeight.SemiBold,
                        color = NovoraInk,
                    )
                    StatusPill(
                        label = DisplayText.orDash(log.status),
                        kind = statusKindFor(log.status),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "In",
                        value = WorkRepository.formatClock(log.checkInTime),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCell(
                        label = "Out",
                        value = WorkRepository.formatClock(log.checkOutTime),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCell(
                        label = "Hours",
                        value = log.workHours?.let { "%.2f".format(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
