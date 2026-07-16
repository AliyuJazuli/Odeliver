package com.hydr.odeliver

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun isDarkTheme(): Boolean {
        // App Priority: Default to Light (false) if the user hasn't explicitly set it.
        // We ignore the system theme entirely unless you want a "Follow System" option later.
        return prefs.getBoolean("dark_theme", false)
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }
}
