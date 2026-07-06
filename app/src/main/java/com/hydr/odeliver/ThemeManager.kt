package com.hydr.odeliver

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun isDarkTheme(systemInDarkTheme: Boolean): Boolean {
        // Return saved preference, default to system theme if not set
        return prefs.getBoolean("dark_theme", systemInDarkTheme)
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }
}
