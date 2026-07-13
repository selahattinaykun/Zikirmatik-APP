package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val displayName: String) {
    GREEN("Yeşil (Klasik)"), 
    BLUE("Mavi"), 
    PURPLE("Mor"), 
    BROWN("Kahverengi")
}

class ThemeRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _currentTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString("selected_theme", AppTheme.GREEN.name) ?: AppTheme.GREEN.name)
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString("selected_theme", theme.name).apply()
        _currentTheme.value = theme
    }
}
