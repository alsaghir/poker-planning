package com.github.alsaghir.pokerplanning.domain


import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemeRepo : ThemeRepo {

    private val defaultTheme = ThemeDto(
        colorValue = Color(0xFF6650A4).value,
        paletteStyle = SerializablePaletteStyle.EXPRESSIVE,
        mode = ThemeMode.DARK
    )

    private val _themeState = MutableStateFlow(defaultTheme)
    override val themeState: StateFlow<ThemeDto> = _themeState.asStateFlow()

    var loadThemeShouldThrow = false
    var saveThemeShouldThrow = false
    var lastSavedTheme: ThemeDto? = null

    override suspend fun loadTheme() {
        if (loadThemeShouldThrow) error("Simulated loadTheme failure")
        // default already in state — nothing to do unless you want to simulate a load
    }

    override suspend fun saveTheme(theme: ThemeDto) {
        if (saveThemeShouldThrow) error("Simulated saveTheme failure")
        lastSavedTheme = theme
        _themeState.value = theme
    }
}