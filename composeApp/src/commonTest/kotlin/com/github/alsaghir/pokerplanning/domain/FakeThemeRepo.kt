package com.github.alsaghir.pokerplanning.domain


import androidx.compose.ui.graphics.Color

class FakeThemeRepo : ThemeRepo {

    private val defaultTheme = ThemeDto(
        colorValue = Color(0xFF6650A4).value,
        paletteStyle = SerializablePaletteStyle.EXPRESSIVE,
        mode = ThemeMode.DARK
    )


    var getThemeShouldThrow = false
    var saveThemeShouldThrow = false
    var lastSavedTheme: ThemeDto? = null

    override suspend fun getTheme(): ThemeDto {
        if (getThemeShouldThrow)
            error("Simulated theme loading failure")
        else
            return lastSavedTheme ?: defaultTheme
    }

    override suspend fun saveTheme(theme: ThemeDto) {
        if (saveThemeShouldThrow) error("Simulated saveTheme failure")
        lastSavedTheme = theme
    }
}