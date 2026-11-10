package dev.tint_wym.novora_hrms_android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = NovoraBlueLight,
    onPrimary = NovoraBlueDark,
    primaryContainer = NovoraPrimaryContainerDark,
    onPrimaryContainer = NovoraPrimaryContainer,
    secondary = NovoraSecondaryContainer,
    onSecondary = NovoraInk,
    secondaryContainer = NovoraSurfaceVariantDark,
    onSecondaryContainer = NovoraSecondaryContainer,
    tertiary = NovoraTertiaryContainer,
    onTertiary = NovoraOnTertiaryContainer,
    background = NovoraBackgroundDark,
    onBackground = NovoraSurface,
    surface = NovoraSurfaceDark,
    onSurface = NovoraSurface,
    surfaceVariant = NovoraSurfaceVariantDark,
    onSurfaceVariant = NovoraBorder,
    outline = NovoraOutline,
    outlineVariant = NovoraSurfaceVariantDark,
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
)

private val LightColorScheme = lightColorScheme(
    primary = NovoraBlue,
    onPrimary = Color.White,
    primaryContainer = NovoraPrimaryContainer,
    onPrimaryContainer = NovoraOnPrimaryContainer,
    secondary = NovoraSecondary,
    onSecondary = Color.White,
    secondaryContainer = NovoraSecondaryContainer,
    onSecondaryContainer = NovoraInk,
    tertiary = NovoraTertiary,
    onTertiary = Color.White,
    tertiaryContainer = NovoraTertiaryContainer,
    onTertiaryContainer = NovoraOnTertiaryContainer,
    background = NovoraSurface,
    onBackground = NovoraInk,
    surface = NovoraCard,
    onSurface = NovoraInk,
    surfaceVariant = NovoraSurfaceVariant,
    onSurfaceVariant = NovoraSecondary,
    outline = NovoraOutline,
    outlineVariant = NovoraBorder,
    error = NovoraDanger,
    errorContainer = NovoraErrorContainer,
    onErrorContainer = Color(0xFF9F1239),
)

private val NovoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun NovoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NovoraShapes,
        content = content,
    )
}
