package dev.tint_wym.novora_hrms_android.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.AttendanceItem
import dev.tint_wym.novora_hrms_android.core.model.HomeSnapshot
import dev.tint_wym.novora_hrms_android.core.model.KpiItem
import dev.tint_wym.novora_hrms_android.core.model.OnboardingTask
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlueDark
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlueLight
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraCard
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraDanger
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraHeroAccent
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraHeroMuted
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInfoSoft
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraLiveDot
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSky
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSkySoft
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccess
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccessBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccessSoft
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraWarn
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraWarnSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    user: SessionUser,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var snapshot by remember { mutableStateOf<HomeSnapshot?>(null) }
    var todayLog by remember { mutableStateOf<AttendanceItem?>(null) }
    var attendanceDays by remember { mutableStateOf(0) }
    var feeds by remember { mutableStateOf<List<dev.tint_wym.novora_hrms_android.core.model.FeedPost>>(emptyList()) }
    var holidays by remember { mutableStateOf<List<dev.tint_wym.novora_hrms_android.core.model.HolidayItem>>(emptyList()) }
    var unreadNotifications by remember { mutableStateOf(0) }
    var onboarding by remember { mutableStateOf<List<OnboardingTask>>(emptyList()) }

    val today = remember { WorkRepository.todayIso() }
    val pendingLeave = snapshot?.leaveRequests?.count { it.status == "PENDING" } ?: 0

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val (home, logs) = withContext(Dispatchers.IO) {
                    coroutineScope {
                        val homeDeferred = async { repo.fetchHome() }
                        val logsDeferred = async { repo.fetchMyAttendance() }
                        homeDeferred.await() to logsDeferred.await()
                    }
                }
                snapshot = home
                todayLog = logs.firstOrNull { it.workDate == today }
                attendanceDays = logs.count { it.checkInTime != null }

                val extras = repo.fetchHomeExtras()
                feeds = extras.first.take(5)
                holidays = extras.second
                    .filter { it.holidayDate >= today }
                    .take(5)
                unreadNotifications = withContext(Dispatchers.IO) {
                    try {
                        repo.fetchNotifications().count { !it.read }
                    } catch (_: Exception) {
                        0
                    }
                }
                onboarding = withContext(Dispatchers.IO) {
                    try {
                        repo.fetchOnboarding()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load home"
            } finally {
                loading = false
            }
        }
    }

    fun punch(checkIn: Boolean) {
        scope.launch {
            busy = true
            message = null
            error = null
            val wasCheckedIn = todayLog?.checkInTime != null
            try {
                val item = withContext(Dispatchers.IO) {
                    if (checkIn) repo.checkIn() else repo.checkOut()
                }
                todayLog = item
                if (checkIn && !wasCheckedIn) {
                    attendanceDays += 1
                }
                message = if (checkIn) "Checked in successfully" else "Checked out successfully"
            } catch (e: Exception) {
                error = e.message ?: "Punch failed"
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    when {
        loading && snapshot == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NovoraSurface),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = NovoraBlue)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NovoraSurface),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 8 },
                    ) {
                        HomeHeader(
                            user = user,
                            unreadCount = unreadNotifications,
                            onOpenNotifications = { onNavigate("notifications") },
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = error != null || message != null,
                        enter = fadeIn() + slideInVertically { -it / 6 },
                        exit = fadeOut(),
                    ) {
                        StatusBanner(
                            error = error,
                            message = message,
                            onRetry = { refresh() },
                        )
                    }
                }

                if (user.canApprove) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically { it / 10 },
                        ) {
                            WorkforceHeroBanner(
                                attendanceRate = snapshot?.attendanceRate,
                            openRequests = DisplayText.orDash(
                                snapshot?.kpis
                                    ?.firstOrNull { it.label.contains("request", ignoreCase = true) }
                                    ?.value
                                    ?: pendingLeave.toString(),
                            ),
                            headcount = DisplayText.orDash(
                                snapshot?.kpis
                                    ?.firstOrNull {
                                        it.label.contains("head", ignoreCase = true) ||
                                            it.label.contains("employee", ignoreCase = true) ||
                                            it.label.contains("team", ignoreCase = true)
                                    }
                                    ?.value,
                            ),
                            kpis = snapshot?.kpis.orEmpty().take(3).map {
                                it.copy(
                                    label = DisplayText.orDash(it.label),
                                    value = DisplayText.orDash(it.value),
                                    delta = DisplayText.orEmpty(it.delta),
                                )
                            },
                                onViewDetails = { onNavigate("attendance") },
                            )
                        }
                    }
                } else {
                    item {
                        EmployeeDayHero(
                            todayLog = todayLog,
                            pendingLeave = pendingLeave,
                            attendanceDays = attendanceDays,
                            onOpenAttendance = { onNavigate("attendance") },
                            onOpenLeave = { onNavigate("leave") },
                        )
                    }
                }

                item {
                    AttendanceClockCard(
                        today = today,
                        todayLog = todayLog,
                        busy = busy,
                        onCheckIn = { punch(true) },
                        onCheckOut = { punch(false) },
                        onOpenAttendance = { onNavigate("attendance") },
                    )
                }

                if (onboarding.isNotEmpty()) {
                    item { SectionLabel(title = "Onboarding") }
                    item {
                        OnboardingCard(
                            tasks = onboarding,
                            onToggle = { task, completed ->
                                scope.launch {
                                    try {
                                        val updated = withContext(Dispatchers.IO) {
                                            repo.setOnboardingCompleted(task.id, completed)
                                        }
                                        onboarding = onboarding.map {
                                            if (it.id == task.id) updated else it
                                        }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not update onboarding"
                                    }
                                }
                            },
                        )
                    }
                }

                item {
                    SectionLabel(title = "Shortcuts")
                }

                item {
                    QuickActionsGrid(
                        canApprove = user.canApprove,
                        attendanceDays = attendanceDays,
                        leaveCount = snapshot?.leaveRequests?.size ?: 0,
                        pendingLeave = pendingLeave,
                        onNavigate = onNavigate,
                    )
                }

                if (feeds.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionLabel(title = "Announcements")
                        }
                    }
                    items(feeds) { post ->
                        AnnouncementCard(post = post)
                    }
                }

                if (holidays.isNotEmpty()) {
                    item { SectionLabel(title = "Upcoming holidays") }
                    item { HolidaysStrip(holidays = holidays) }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionLabel(title = "My leave")
                        TextButton(onClick = { onNavigate("leave") }) {
                            Text("View all", color = NovoraBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (snapshot?.leaveRequests.isNullOrEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NovoraCard),
                            border = BorderStroke(1.dp, NovoraBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Text(
                                text = "No leave requests yet.",
                                color = NovoraSlate,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                } else {
                    items(snapshot!!.leaveRequests.take(5)) { leave ->
                        LeavePreviewCard(
                            title = DisplayText.orFallback(leave.leaveType, "Leave"),
                            status = DisplayText.orDash(leave.status),
                            dateRange = DisplayText.orDash(leave.dateRange),
                            onClick = { onNavigate("leave") },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = NovoraInk,
    )
}

@Composable
private fun StatusBanner(
    error: String?,
    message: String?,
    onRetry: () -> Unit,
) {
    when {
        error != null -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NovoraDanger.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NovoraDanger.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = error,
                        color = NovoraDanger,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = onRetry) { Text("Retry") }
                }
            }
        }
        message != null -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NovoraSuccessSoft,
                border = BorderStroke(1.dp, NovoraSuccessBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = message,
                    color = NovoraSuccess,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    user: SessionUser,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
) {
    val initials = user.displayName
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "N" }
    val roleLabel = user.primaryRole?.replace('_', ' ') ?: "EMPLOYEE"
    val org = user.organizationName?.trim().orEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(NovoraBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                color = NovoraBlue,
                fontSize = 18.sp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodySmall,
                color = NovoraSlate,
            )
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NovoraInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NovoraBlue.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = roleLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NovoraBlueDark,
                        maxLines = 1,
                    )
                }
                if (org.isNotBlank()) {
                    Text(
                        text = org,
                        style = MaterialTheme.typography.labelSmall,
                        color = NovoraSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NovoraCard)
                .border(1.dp, NovoraBorder, CircleShape)
                .clickable(onClick = onOpenNotifications),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = if (unreadCount > 0) NovoraBlue else NovoraSlate,
                modifier = Modifier.size(20.dp),
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NovoraDanger),
                )
            }
        }
    }
}

@Composable
private fun AnnouncementCard(post: dev.tint_wym.novora_hrms_android.core.model.FeedPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(NovoraBlue),
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = DisplayText.orDash(post.title),
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DisplayText.orDash(post.body),
                    color = NovoraSlate,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = listOf(post.authorName, post.createdAt.take(10))
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = NovoraSlate,
                )
            }
        }
    }
}

@Composable
private fun HolidaysStrip(holidays: List<dev.tint_wym.novora_hrms_android.core.model.HolidayItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            holidays.forEach { holiday ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NovoraPrimaryContainer,
                    ) {
                        Text(
                            text = holiday.holidayDate.takeLast(5),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            color = NovoraBlue,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DisplayText.orDash(holiday.name),
                            fontWeight = FontWeight.SemiBold,
                            color = NovoraInk,
                        )
                        if (holiday.type.isNotBlank()) {
                            Text(
                                text = holiday.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = NovoraSlate,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeDayHero(
    todayLog: AttendanceItem?,
    pendingLeave: Int,
    attendanceDays: Int,
    onOpenAttendance: () -> Unit,
    onOpenLeave: () -> Unit,
) {
    val punchedIn = todayLog?.checkInTime != null
    val punchedOut = todayLog?.checkOutTime != null
    val statusLabel = when {
        punchedOut -> "Shift done"
        punchedIn -> "On shift"
        else -> "Not punched"
    }
    val statusValue = when {
                        punchedOut -> "Out ${WorkRepository.formatClock(todayLog?.checkOutTime)}"
                        punchedIn -> "In ${WorkRepository.formatClock(todayLog?.checkInTime)}"
                        else -> "Ready to check in"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NovoraBlueDark, NovoraBlue, NovoraBlueLight),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "YOUR DAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp,
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DayStatChip(
                        label = statusLabel,
                        value = statusValue,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenAttendance),
                    )
                    DayStatChip(
                        label = "Pending leave",
                        value = pendingLeave.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenLeave),
                    )
                    DayStatChip(
                        label = "Days logged",
                        value = attendanceDays.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenAttendance),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.14f),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NovoraHeroMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WorkforceHeroBanner(
    attendanceRate: Double?,
    openRequests: String,
    headcount: String,
    kpis: List<KpiItem>,
    onViewDetails: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NovoraBlueDark, NovoraBlue, NovoraBlueLight),
                    ),
                )
                .padding(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 28.dp, y = (-28).dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NovoraLiveDot),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE WORKFORCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp,
                                ),
                                color = Color.White,
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = NovoraHeroMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroStat(
                        value = attendanceRate?.let { "${"%.0f".format(it)}%" } ?: "—",
                        label = "Attendance",
                        modifier = Modifier.weight(1f),
                    )
                    HeroStat(
                        value = openRequests,
                        label = "Requests",
                        accent = NovoraHeroAccent,
                        modifier = Modifier.weight(1f),
                    )
                    HeroStat(
                        value = headcount,
                        label = "Headcount",
                        modifier = Modifier.weight(1f),
                    )
                }

                if (kpis.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        kpis.take(2).forEach { kpi ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = DisplayText.orDash(kpi.label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovoraHeroMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = DisplayText.orDash(kpi.value),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "View detailed logs →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NovoraHeroMuted,
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    accent: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NovoraHeroMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AttendanceClockCard(
    today: String,
    todayLog: AttendanceItem?,
    busy: Boolean,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onOpenAttendance: () -> Unit,
) {
    val punchedIn = todayLog?.checkInTime != null
    val punchedOut = todayLog?.checkOutTime != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenAttendance),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                val iconBg = when {
                    punchedOut -> NovoraSuccessSoft
                    punchedIn -> NovoraInfoSoft
                    else -> Color(0xFFF1F5F9)
                }
                val iconTint = when {
                    punchedOut -> NovoraSuccess
                    punchedIn -> NovoraBlue
                    else -> NovoraSlate
                }
                val icon = when {
                    punchedOut -> Icons.Filled.DoneAll
                    punchedIn -> Icons.Filled.AccessTimeFilled
                    else -> Icons.Filled.Fingerprint
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = iconTint,
                        )
                    } else {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when {
                            punchedOut -> "Shift completed"
                            punchedIn -> "Shift in progress"
                            else -> "Daily attendance"
                        },
                        fontWeight = FontWeight.Bold,
                        color = NovoraInk,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = when {
                            punchedOut -> "Out ${WorkRepository.formatClock(todayLog?.checkOutTime)} · $today"
                            punchedIn -> "In ${WorkRepository.formatClock(todayLog?.checkInTime)} · Tap for details"
                            else -> "Today · $today · Ready to punch"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = NovoraSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            when {
                !punchedIn -> {
                    Button(
                        onClick = onCheckIn,
                        enabled = !busy,
                        modifier = Modifier.height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovoraBlue),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Check In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                !punchedOut -> {
                    Button(
                        onClick = onCheckOut,
                        enabled = !busy,
                        modifier = Modifier.height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovoraDanger),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Check Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NovoraSuccessSoft,
                        border = BorderStroke(1.dp, NovoraSuccessBorder),
                    ) {
                        Text(
                            text = "PUNCHED",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NovoraSuccess,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    canApprove: Boolean,
    attendanceDays: Int,
    leaveCount: Int,
    pendingLeave: Int,
    onNavigate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickTile(
                title = "Attendance",
                subtitle = "$attendanceDays days logged",
                icon = Icons.Filled.Schedule,
                iconTint = NovoraBlue,
                iconBg = NovoraInfoSoft,
                onClick = { onNavigate("attendance") },
                modifier = Modifier.weight(1f),
            )
            QuickTile(
                title = "Leave",
                subtitle = if (leaveCount == 0) "Request leave" else "$leaveCount requests",
                icon = Icons.Filled.BeachAccess,
                iconTint = NovoraSuccess,
                iconBg = NovoraSuccessSoft,
                onClick = { onNavigate("leave") },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickTile(
                title = "Payroll",
                subtitle = "View payslips",
                icon = Icons.Filled.Payments,
                iconTint = NovoraWarn,
                iconBg = NovoraWarnSoft,
                onClick = { onNavigate("payroll") },
                modifier = Modifier.weight(1f),
            )
            QuickTile(
                title = "Claims",
                subtitle = "Expense claims",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconTint = NovoraWarn,
                iconBg = NovoraWarnSoft,
                onClick = { onNavigate("claims") },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickTile(
                title = "Helpdesk",
                subtitle = "Support tickets",
                icon = Icons.Filled.SupportAgent,
                iconTint = NovoraSky,
                iconBg = NovoraSkySoft,
                onClick = { onNavigate("helpdesk") },
                modifier = Modifier.weight(1f),
            )
            QuickTile(
                title = "Alerts",
                subtitle = if (pendingLeave > 0) "$pendingLeave pending" else "Notifications",
                icon = Icons.Outlined.Notifications,
                iconTint = NovoraSky,
                iconBg = NovoraSkySoft,
                onClick = { onNavigate("notifications") },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickTile(
                title = "Profile",
                subtitle = "Account & documents",
                icon = Icons.Filled.Person,
                iconTint = NovoraSky,
                iconBg = NovoraSkySoft,
                onClick = { onNavigate("profile") },
                modifier = Modifier.weight(1f),
            )
            if (canApprove) {
                QuickTile(
                    title = "Approvals",
                    subtitle = if (pendingLeave > 0) "$pendingLeave pending" else "Review leave",
                    icon = Icons.Filled.Approval,
                    iconTint = NovoraWarn,
                    iconBg = NovoraWarnSoft,
                    onClick = { onNavigate("approvals") },
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OnboardingCard(
    tasks: List<OnboardingTask>,
    onToggle: (OnboardingTask, Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${tasks.count { it.completed }}/${tasks.size} complete",
                style = MaterialTheme.typography.labelMedium,
                color = NovoraSlate,
            )
            tasks.forEach { task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { onToggle(task, it) },
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DisplayText.orDash(task.title),
                            fontWeight = FontWeight.SemiBold,
                            color = NovoraInk,
                        )
                        if (DisplayText.orEmpty(task.dueDate).isNotBlank()) {
                            Text(
                                text = "Due ${task.dueDate.take(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovoraSlate,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 108.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = NovoraInk)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovoraSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LeavePreviewCard(
    title: String,
    status: String,
    dateRange: String,
    onClick: () -> Unit,
) {
    val (chipBg, chipFg) = leaveStatusColors(status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovoraCard),
        border = BorderStroke(1.dp, NovoraBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = NovoraInk)
                Text(
                    dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovoraSlate,
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = chipBg,
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = chipFg,
                )
            }
        }
    }
}

private fun leaveStatusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "APPROVED" -> NovoraSuccessSoft to NovoraSuccess
        "REJECTED", "CANCELLED" -> NovoraDanger.copy(alpha = 0.08f) to NovoraDanger
        "PENDING" -> NovoraWarnSoft to NovoraWarn
        else -> NovoraInfoSoft to NovoraBlue
    }
}
