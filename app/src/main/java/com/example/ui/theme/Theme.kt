package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = ForestEmerald,
    onPrimary = Color.White,
    primaryContainer = MintSurface,
    onPrimaryContainer = ForestEmeraldDark,
    secondary = WarmAmber,
    onSecondary = Color.White,
    secondaryContainer = WarmAmberSoft,
    onSecondaryContainer = Color(0xFF7D3D04),
    tertiary = SkyGem,
    onTertiary = Color.White,
    tertiaryContainer = SkyGemSoft,
    onTertiaryContainer = Color(0xFF0E477D),
    background = AlabasterBg,
    onBackground = InkCharcoal,
    surface = AlabasterBg,
    onSurface = InkCharcoal,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = SageMuted,
    outline = ParchmentBorder,
    outlineVariant = MintBorder,
    error = CoralHeart,
    onError = Color.White,
    errorContainer = CoralHeartSoft,
    onErrorContainer = Color(0xFF8D1A28)
)

private val DarkColors = darkColorScheme(
    primary = ForestEmeraldLight,
    onPrimary = Color(0xFF082412),
    primaryContainer = DarkMintSurface,
    onPrimaryContainer = MintSurfaceStrong,
    secondary = WarmAmber,
    onSecondary = Color(0xFF2B1300),
    secondaryContainer = Color(0xFF42270E),
    onSecondaryContainer = WarmAmberSoft,
    tertiary = SkyGem,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF132D47),
    onTertiaryContainer = SkyGemSoft,
    background = DarkSlateBg,
    onBackground = DarkTextPrimary,
    surface = DarkSlateBg,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkCardBorder,
    outlineVariant = DarkMintBorder,
    error = CoralHeart,
    onError = Color.White,
    errorContainer = Color(0xFF47181E),
    onErrorContainer = CoralHeartSoft
)

val GuruQuestShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GuruQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GuruQuestTypography,
        shapes = GuruQuestShapes,
        content = content
    )
}
