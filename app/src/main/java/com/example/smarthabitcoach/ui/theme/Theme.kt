package com.example.smarthabitcoach.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary                = HabitGreen40,
    onPrimary              = Color.White,
    primaryContainer       = HabitGreen90,
    onPrimaryContainer     = HabitGreen10,
    secondary              = HabitTeal40,
    onSecondary            = Color.White,
    secondaryContainer     = HabitTeal90,
    onSecondaryContainer   = Color(0xFF001F22),
    tertiary               = StreakAmber40,
    onTertiary             = Color.White,
    tertiaryContainer      = StreakAmber90,
    onTertiaryContainer    = Color(0xFF271900),
    error                  = ErrorRed40,
    onError                = Color.White,
    errorContainer         = ErrorRed90,
    onErrorContainer       = Color(0xFF410002),
    background             = Neutral99,
    onBackground           = Neutral10,
    surface                = Neutral99,
    onSurface              = Neutral10,
    surfaceVariant         = NeutralVar90,
    onSurfaceVariant       = NeutralVar30,
    outline                = NeutralVar50,
    outlineVariant         = NeutralVar80,
    surfaceContainerHighest = Neutral90,
    surfaceContainerHigh   = Neutral95,
    surfaceContainer       = Color(0xFFECEDF0),
)

private val DarkColorScheme = darkColorScheme(
    primary                = HabitGreen80,
    onPrimary              = HabitGreen20,
    primaryContainer       = HabitGreen30,
    onPrimaryContainer     = HabitGreen90,
    secondary              = HabitTeal80,
    onSecondary            = Color(0xFF003739),
    secondaryContainer     = Color(0xFF004F53),
    onSecondaryContainer   = HabitTeal90,
    tertiary               = StreakAmber80,
    onTertiary             = Color(0xFF412D00),
    tertiaryContainer      = Color(0xFF5C4200),
    onTertiaryContainer    = StreakAmber90,
    error                  = ErrorRed80,
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = ErrorRed90,
    background             = Neutral10,
    onBackground           = Neutral90,
    surface                = Neutral10,
    onSurface              = Neutral90,
    surfaceVariant         = NeutralVar30,
    onSurfaceVariant       = NeutralVar80,
    outline                = NeutralVar50,
    outlineVariant         = NeutralVar30,
    surfaceContainerHighest = Neutral20,
    surfaceContainerHigh   = Color(0xFF262729),
    surfaceContainer       = Color(0xFF1E2022),
)

@Composable
fun SmartHabitCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Edge-to-edge status bar tint
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}