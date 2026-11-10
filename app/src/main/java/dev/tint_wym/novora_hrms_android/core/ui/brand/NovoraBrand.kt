package dev.tint_wym.novora_hrms_android.core.ui.brand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LogoCyan = Color(0xFF00A7E1)
private val LogoTeal = Color(0xFF00B2A9)
private val LogoOrange = Color(0xFFFF9800)
private val LogoOrangeDeep = Color(0xFFF57C00)
private val LogoBlue = Color(0xFF0A58A4)
private val LogoSlate = Color(0xFFE2E8F0)
private val LogoSlateDeep = Color(0xFFCBD5E1)
private val WordInk = Color(0xFF0F172A)

/** Official Novora people / swoosh mark (matches web + iOS). */
@Composable
fun NovoraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val sx = this.size.width / 100f
        val sy = this.size.height / 100f

        fun o(x: Float, y: Float) = Offset(x * sx, y * sy)

        // Back person
        drawOval(
            color = LogoSlate,
            topLeft = o(34f, 15f),
            size = Size(16f * sx, 16f * sy),
        )
        drawPath(
            path = Path().apply {
                moveTo(42f * sx, 35f * sy)
                cubicTo(33f * sx, 39f * sy, 36f * sx, 50f * sy, 48f * sx, 53f * sy)
                cubicTo(48f * sx, 53f * sy, 49f * sx, 43f * sy, 42f * sx, 35f * sy)
                close()
            },
            color = LogoSlateDeep,
        )

        // Left cyan person
        drawOval(
            color = LogoCyan,
            topLeft = o(16f, 20f),
            size = Size(16f * sx, 16f * sy),
        )
        drawPath(
            path = Path().apply {
                moveTo(12f * sx, 70f * sy)
                cubicTo(14f * sx, 55f * sy, 18f * sx, 40f * sy, 28f * sx, 38f * sy)
                cubicTo(38f * sx, 36f * sy, 42f * sx, 50f * sy, 48f * sx, 58f * sy)
                cubicTo(55f * sx, 67f * sy, 60f * sx, 70f * sy, 68f * sx, 62f * sy)
                cubicTo(60f * sx, 74f * sy, 45f * sx, 74f * sy, 38f * sx, 66f * sy)
                cubicTo(30f * sx, 57f * sy, 24f * sx, 45f * sy, 18f * sx, 56f * sy)
                cubicTo(14f * sx, 62f * sy, 13f * sx, 67f * sy, 12f * sx, 70f * sy)
                close()
            },
            color = LogoTeal,
        )

        // Right orange / blue swoosh
        drawOval(
            color = LogoOrange,
            topLeft = o(60f, 19f),
            size = Size(16f * sx, 16f * sy),
        )
        drawPath(
            path = Path().apply {
                moveTo(68f * sx, 35f * sy)
                cubicTo(58f * sx, 45f * sy, 52f * sx, 58f * sy, 52f * sx, 68f * sy)
                cubicTo(52f * sx, 75f * sy, 58f * sx, 78f * sy, 64f * sx, 70f * sy)
                cubicTo(70f * sx, 60f * sy, 78f * sx, 45f * sy, 88f * sx, 25f * sy)
                cubicTo(80f * sx, 23f * sy, 72f * sx, 28f * sy, 68f * sx, 35f * sy)
                close()
            },
            color = LogoOrangeDeep,
        )
        drawPath(
            path = Path().apply {
                moveTo(52f * sx, 68f * sy)
                cubicTo(52f * sx, 75f * sy, 58f * sx, 84f * sy, 68f * sx, 84f * sy)
                cubicTo(78f * sx, 84f * sy, 82f * sx, 72f * sy, 88f * sx, 50f * sy)
                cubicTo(85f * sx, 64f * sy, 78f * sx, 74f * sy, 68f * sx, 74f * sy)
                cubicTo(62f * sx, 74f * sy, 56f * sx, 71f * sy, 52f * sx, 68f * sy)
                close()
            },
            color = LogoBlue,
        )
    }
}

@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 40.dp,
    subtitle: String = "HRMS",
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NovoraLogo(size = logoSize)
        Column {
            Text(
                text = "Novora",
                style = titleStyle,
                fontWeight = FontWeight.Black,
                color = WordInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LogoBlue,
                letterSpacing = 1.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
