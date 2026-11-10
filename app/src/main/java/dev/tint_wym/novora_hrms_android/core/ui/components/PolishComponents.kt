package dev.tint_wym.novora_hrms_android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraCard
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraDanger
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraErrorContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraPrimaryContainer
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccess
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSuccessSoft
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraWarn
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraWarnSoft

@Composable
fun PolishCard(
    modifier: Modifier = Modifier,
    radius: Dp = 18.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        color = NovoraCard,
        border = BorderStroke(1.dp, NovoraBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = NovoraInk,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = NovoraSlate,
        )
    }
}

@Composable
fun SectionLabel(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = NovoraInk,
    )
}

@Composable
fun StatusPill(
    label: String,
    kind: StatusKind,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, border) = when (kind) {
        StatusKind.Success -> Triple(NovoraSuccessSoft, NovoraSuccess, Color(0xFFA7F3D0))
        StatusKind.Warning -> Triple(NovoraWarnSoft, NovoraWarn, Color(0xFFFDE68A))
        StatusKind.Danger -> Triple(NovoraErrorContainer, NovoraDanger, Color(0xFFFECDD3))
        StatusKind.Info -> Triple(NovoraPrimaryContainer, NovoraBlue, Color(0xFFC7D2FE))
        StatusKind.Neutral -> Triple(Color(0xFFF1F5F9), NovoraSlate, NovoraBorder)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg,
                fontSize = 11.sp,
            )
        }
    }
}

enum class StatusKind { Success, Warning, Danger, Info, Neutral }

fun statusKindFor(raw: String?): StatusKind {
    val value = raw.orEmpty().trim().lowercase()
    return when {
        value.contains("paid") || value.contains("approved") || value.contains("present") ||
            value.contains("processed") || value.contains("success") -> StatusKind.Success
        value.contains("pending") || value.contains("draft") || value.contains("late") -> StatusKind.Warning
        value.contains("reject") || value.contains("cancel") || value.contains("absent") ||
            value.contains("error") -> StatusKind.Danger
        else -> StatusKind.Info
    }
}

@Composable
fun SoftBanner(
    text: String,
    kind: StatusKind,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, border) = when (kind) {
        StatusKind.Success -> Triple(NovoraSuccessSoft, NovoraSuccess, Color(0xFFA7F3D0))
        StatusKind.Warning -> Triple(NovoraWarnSoft, NovoraWarn, Color(0xFFFDE68A))
        StatusKind.Danger -> Triple(NovoraErrorContainer, NovoraDanger, Color(0xFFFECDD3))
        StatusKind.Info -> Triple(NovoraPrimaryContainer, NovoraBlue, Color(0xFFC7D2FE))
        StatusKind.Neutral -> Triple(Color(0xFFF1F5F9), NovoraSlate, NovoraBorder)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
fun IconWell(
    icon: ImageVector,
    tint: Color = NovoraBlue,
    background: Color = NovoraPrimaryContainer,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NovoraBlue,
            contentColor = Color.White,
            disabledContainerColor = NovoraBlue.copy(alpha = 0.4f),
        ),
    ) {
        Text(text = if (loading) "Please wait…" else text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NovoraBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NovoraInk),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    PolishCard(modifier = modifier, contentPadding = 28.dp) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NovoraInk,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = NovoraSlate,
        )
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    size: Dp = 40.dp,
) {
    val initials = name
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "N" }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(NovoraPrimaryContainer)
            .border(1.5.dp, Color(0xFFC7D2FE), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            color = NovoraBlue,
            fontSize = (size.value * 0.35f).sp,
        )
    }
}

@Composable
fun MetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NovoraSlate,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            fontSize = 10.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NovoraInk,
        )
    }
}

@Composable
fun HorizontalSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}
