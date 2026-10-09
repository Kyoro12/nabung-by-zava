package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getAppColorScheme(darkTheme: Boolean, accentColor: String): ColorScheme {
    return when (accentColor.uppercase()) {
        "PINK" -> if (darkTheme) {
            darkColorScheme(
                primary = PinkPrimaryDark,
                onPrimary = PinkOnPrimaryDark,
                primaryContainer = PinkPrimaryContainerDark,
                onPrimaryContainer = PinkOnPrimaryContainerDark,
                secondary = PinkPrimaryDark,
                tertiary = PurplePrimaryDark,
                background = Color(0xFF140C0E),
                surface = Color(0xFF1F1417),
                surfaceVariant = Color(0xFF332025)
            )
        } else {
            lightColorScheme(
                primary = PinkPrimaryLight,
                onPrimary = PinkOnPrimaryLight,
                primaryContainer = PinkPrimaryContainerLight,
                onPrimaryContainer = PinkOnPrimaryContainerLight,
                secondary = PinkPrimaryLight,
                tertiary = PurplePrimaryLight,
                background = Color(0xFFFFF8F8),
                surface = Color(0xFFFFF8F8),
                surfaceVariant = Color(0xFFFCE4EC)
            )
        }
        "BLUE" -> if (darkTheme) {
            darkColorScheme(
                primary = BluePrimaryDark,
                onPrimary = BlueOnPrimaryDark,
                primaryContainer = BluePrimaryContainerDark,
                onPrimaryContainer = BlueOnPrimaryContainerDark,
                secondary = BluePrimaryDark,
                tertiary = GreenPrimaryDark,
                background = Color(0xFF0C131D),
                surface = Color(0xFF131D2C),
                surfaceVariant = Color(0xFF1E2D42)
            )
        } else {
            lightColorScheme(
                primary = BluePrimaryLight,
                onPrimary = BlueOnPrimaryLight,
                primaryContainer = BluePrimaryContainerLight,
                onPrimaryContainer = BlueOnPrimaryContainerLight,
                secondary = BluePrimaryLight,
                tertiary = GreenPrimaryLight,
                background = Color(0xFFF8FAFD),
                surface = Color(0xFFF8FAFD),
                surfaceVariant = Color(0xFFE2EAF8)
            )
        }
        "GREEN" -> if (darkTheme) {
            darkColorScheme(
                primary = GreenPrimaryDark,
                onPrimary = GreenOnPrimaryDark,
                primaryContainer = GreenPrimaryContainerDark,
                onPrimaryContainer = GreenOnPrimaryContainerDark,
                secondary = GreenPrimaryDark,
                tertiary = BluePrimaryDark,
                background = Color(0xFF0C1610),
                surface = Color(0xFF132219),
                surfaceVariant = Color(0xFF1C3426)
            )
        } else {
            lightColorScheme(
                primary = GreenPrimaryLight,
                onPrimary = GreenOnPrimaryLight,
                primaryContainer = GreenPrimaryContainerLight,
                onPrimaryContainer = GreenOnPrimaryContainerLight,
                secondary = GreenPrimaryLight,
                tertiary = BluePrimaryLight,
                background = Color(0xFFF7FAF8),
                surface = Color(0xFFF7FAF8),
                surfaceVariant = Color(0xFFE1EFE4)
            )
        }
        "BLACK" -> if (darkTheme) {
            darkColorScheme(
                primary = BlackPrimaryDark,
                onPrimary = BlackOnPrimaryDark,
                primaryContainer = BlackPrimaryContainerDark,
                onPrimaryContainer = BlackOnPrimaryContainerDark,
                secondary = BlackPrimaryDark,
                background = Color(0xFF121416),
                surface = Color(0xFF1B1E22),
                surfaceVariant = Color(0xFF2B3037)
            )
        } else {
            lightColorScheme(
                primary = BlackPrimaryLight,
                onPrimary = BlackOnPrimaryLight,
                primaryContainer = BlackPrimaryContainerLight,
                onPrimaryContainer = BlackOnPrimaryContainerLight,
                secondary = BlackPrimaryLight,
                background = Color(0xFFF9FAFB),
                surface = Color(0xFFF9FAFB),
                surfaceVariant = Color(0xFFECEFF1)
            )
        }
        else -> { // "PURPLE" default
            if (darkTheme) {
                darkColorScheme(
                    primary = PurplePrimaryDark,
                    onPrimary = PurpleOnPrimaryDark,
                    primaryContainer = PurplePrimaryContainerDark,
                    onPrimaryContainer = PurpleOnPrimaryContainerDark,
                    secondary = PurplePrimaryDark,
                    tertiary = PinkPrimaryDark,
                    background = Color(0xFF121016),
                    surface = Color(0xFF1C1924),
                    surfaceVariant = Color(0xFF2E293B)
                )
            } else {
                lightColorScheme(
                    primary = PurplePrimaryLight,
                    onPrimary = PurpleOnPrimaryLight,
                    primaryContainer = PurplePrimaryContainerLight,
                    onPrimaryContainer = PurpleOnPrimaryContainerLight,
                    secondary = PurplePrimaryLight,
                    tertiary = PinkPrimaryLight,
                    background = Color(0xFFFBF8FD),
                    surface = Color(0xFFFBF8FD),
                    surfaceVariant = Color(0xFFEDE7F6)
                )
            }
        }
    }
}

@Composable
fun NabungZavaTheme(
    themeMode: String = "SYSTEM",
    accentColor: String = "PURPLE",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val colorScheme = getAppColorScheme(darkTheme = isDark, accentColor = accentColor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
