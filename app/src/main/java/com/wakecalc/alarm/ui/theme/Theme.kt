package com.wakecalc.alarm.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFFD0BCFF)
private val OnPurple = Color(0xFF381E72)
private val PurpleContainer = Color(0xFF4F378B)
private val OnPurpleContainer = Color(0xFFEADDFF)
private val Pink = Color(0xFFEFB8C8)
private val OnPink = Color(0xFF492532)

private val DarkColors = darkColorScheme(
    primary = Purple,
    onPrimary = OnPurple,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    secondary = Pink,
    onSecondary = OnPink,
    tertiary = Pink,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    surfaceContainer = Color(0xFFF3EDF7)
)

@Composable
fun WakeCalcTheme(
    darkTheme: Boolean = true, // this app lives at 6am — dark by default
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme || isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
