package dev.tint_wym.novora_hrms_android.feature.payroll

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.PayslipItem
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.EmptyStateCard
import dev.tint_wym.novora_hrms_android.core.ui.components.PolishCard
import dev.tint_wym.novora_hrms_android.core.ui.components.ScreenHeader
import dev.tint_wym.novora_hrms_android.core.ui.components.SectionLabel
import dev.tint_wym.novora_hrms_android.core.ui.components.SoftBanner
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusKind
import dev.tint_wym.novora_hrms_android.core.ui.components.StatusPill
import dev.tint_wym.novora_hrms_android.core.ui.components.statusKindFor
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlueDark
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PayrollScreen() {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var payslips by remember { mutableStateOf<List<PayslipItem>>(emptyList()) }
    var selected by remember { mutableStateOf<PayslipItem?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                payslips = withContext(Dispatchers.IO) { repo.fetchMyPayslips() }
            } catch (e: Exception) {
                error = DisplayText.orFallback(e.message, "Could not load payslips")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val latest = payslips.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Payroll",
                subtitle = "Your payslips and pay breakdown by month.",
            )
        }

        when {
            loading && payslips.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = NovoraBlue)
                    }
                }
            }

            !error.isNullOrBlank() && payslips.isEmpty() -> {
                item {
                    SoftBanner(text = DisplayText.orEmpty(error), kind = StatusKind.Danger)
                    TextButton(onClick = { refresh() }) { Text("Retry") }
                }
            }

            payslips.isEmpty() -> {
                item {
                    EmptyStateCard(
                        title = "No payslips yet",
                        subtitle = "When HR runs payroll for your organization, your monthly slips will show up here.",
                    )
                }
            }

            else -> {
                if (latest != null) {
                    item { LatestPayHero(latest, onOpen = { selected = latest }) }
                    item { SectionLabel("All payslips") }
                }
                items(payslips, key = { it.id }) { slip ->
                    PayslipCard(slip, onOpen = { selected = slip })
                }
            }
        }
    }

    selected?.let { slip ->
        PayslipDetailDialog(slip = slip, onDismiss = { selected = null })
    }
}

@Composable
private fun LatestPayHero(slip: PayslipItem, onOpen: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(NovoraBlueDark, Color(0xFF312E81), NovoraBlue),
                ),
            )
            .clickable(onClick = onOpen)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Latest net pay",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                StatusPill(
                    label = DisplayText.orDash(slip.status).uppercase(),
                    kind = statusKindFor(slip.status),
                )
            }
            Text(
                text = slip.periodLabel,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = money(slip.netPay),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "Tap for full payslip",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun PayslipCard(slip: PayslipItem, onOpen: () -> Unit) {
    PolishCard(
        contentPadding = 16.dp,
        modifier = Modifier.clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = slip.periodLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                Text(
                    text = "Tap to view slip",
                    style = MaterialTheme.typography.labelMedium,
                    color = NovoraSlate,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = money(slip.netPay),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraBlue,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusPill(label = DisplayText.orDash(slip.status), kind = statusKindFor(slip.status))
            }
        }
    }
}

@Composable
private fun PayslipDetailDialog(slip: PayslipItem, onDismiss: () -> Unit) {
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
            border = BorderStroke(1.dp, NovoraBorder),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Payslip · ${slip.periodLabel}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraInk,
                )
                StatusPill(label = DisplayText.orDash(slip.status), kind = statusKindFor(slip.status))
                Text(
                    text = money(slip.netPay),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NovoraBlue,
                    fontFamily = FontFamily.Monospace,
                )
                HorizontalDivider(color = NovoraBorder)
                MoneyRow("Basic salary", slip.basicSalary)
                MoneyRow("Allowances", slip.allowances)
                MoneyRow("Overtime", slip.overtimePay)
                MoneyRow("Bonus", slip.bonus)
                MoneyRow("Deductions", slip.deductions, negative = true)
                MoneyRow("Tax", slip.tax, negative = true)
                HorizontalDivider(color = NovoraBorder)
                MoneyRow("Net pay", slip.netPay)
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close", color = NovoraBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MoneyRow(label: String, amount: Double, negative: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = NovoraSlate)
        Text(
            text = if (negative && amount > 0) "−${money(amount)}" else money(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = NovoraInk,
        )
    }
}

private fun money(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(value)
}
