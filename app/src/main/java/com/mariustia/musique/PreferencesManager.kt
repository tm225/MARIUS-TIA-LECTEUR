package com.mariustia.musique

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object PreferencesManager {
    private const val PREFS_NAME = "marius_tia_musique_prefs"
    private const val KEY_ACCENT_COLOR = "accent_color"

    // Couleur néon par défaut : cyan "terminal"
    private val defaultColor = 0xFF00E5FF.toInt()

    // État observable partagé dans toute l'app
    val accentColor = mutableStateOf(Color(defaultColor))

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_ACCENT_COLOR, defaultColor)
        accentColor.value = Color(saved)
    }

    fun setAccentColor(context: Context, color: Color) {
        accentColor.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ACCENT_COLOR, color.toArgb()).apply()
    }
}
