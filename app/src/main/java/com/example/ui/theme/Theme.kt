package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppTheme

fun getDarkColorScheme(theme: AppTheme) = darkColorScheme(
    primary = Gold, 
    secondary = when (theme) {
        AppTheme.GREEN -> GreenLight
        AppTheme.BLUE -> BlueLight
        AppTheme.PURPLE -> PurpleLight
        AppTheme.BROWN -> BrownLight
    }, 
    tertiary = GoldLight,
    background = when (theme) {
        AppTheme.GREEN -> GreenDarkBg
        AppTheme.BLUE -> BlueDarkBg
        AppTheme.PURPLE -> PurpleDarkBg
        AppTheme.BROWN -> BrownDarkBg
    },
    surface = when (theme) {
        AppTheme.GREEN -> GreenDark
        AppTheme.BLUE -> BlueDark
        AppTheme.PURPLE -> PurpleDark
        AppTheme.BROWN -> BrownDark
    },
    onPrimary = when (theme) {
        AppTheme.GREEN -> GreenDarkBg
        AppTheme.BLUE -> BlueDarkBg
        AppTheme.PURPLE -> PurpleDarkBg
        AppTheme.BROWN -> BrownDarkBg
    },
    onSecondary = Gold,
    onBackground = GoldLight,
    onSurface = GoldLight
)

fun getLightColorScheme(theme: AppTheme) = lightColorScheme(
    primary = when (theme) {
        AppTheme.GREEN -> GreenDark
        AppTheme.BLUE -> BlueDark
        AppTheme.PURPLE -> PurpleDark
        AppTheme.BROWN -> BrownDark
    },
    secondary = when (theme) {
        AppTheme.GREEN -> GreenLight
        AppTheme.BLUE -> BlueLight
        AppTheme.PURPLE -> PurpleLight
        AppTheme.BROWN -> BrownLight
    },
    tertiary = GoldDark,
    background = Color(0xFFF0F5F2),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = when (theme) {
        AppTheme.GREEN -> GreenDark
        AppTheme.BLUE -> BlueDark
        AppTheme.PURPLE -> PurpleDark
        AppTheme.BROWN -> BrownDark
    },
    onSurface = when (theme) {
        AppTheme.GREEN -> GreenDark
        AppTheme.BLUE -> BlueDark
        AppTheme.PURPLE -> PurpleDark
        AppTheme.BROWN -> BrownDark
    }
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(appTheme)
        else -> getLightColorScheme(appTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
