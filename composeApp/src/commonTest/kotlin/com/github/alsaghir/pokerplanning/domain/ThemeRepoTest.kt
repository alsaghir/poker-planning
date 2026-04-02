package com.github.alsaghir.pokerplanning.domain

import FakeStorage
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ThemeRepoTest {

    private lateinit var fakeStorage: FakeStorage
    private lateinit var themeRepo: ThemeRepo
    private val defaultColor = Color(0xFFFF0000)
    private val themeKey = "theme"

    @BeforeTest
    fun setup() {
        fakeStorage = FakeStorage()
        themeRepo = ThemeRepoImpl(
            storage = fakeStorage,
            defaultColor = defaultColor
        )
    }

    @AfterTest
    fun teardown() {
        fakeStorage.clear()
    }


    @Test
    fun `getTheme with empty storage returns default theme`() = runTest {
        val theme = themeRepo.getTheme()

        assertEquals(ThemeDto.default(defaultColor), theme)
    }


    @Test
    fun `saveTheme persists serialized theme via Storage`() = runTest {
        val newTheme = ThemeDto.fromColor(
            color = Color(0xFF00FF00),
            paletteStyle = PaletteStyle.TonalSpot,
            mode = ThemeMode.LIGHT
        )

        themeRepo.saveTheme(newTheme)

        val savedJson = fakeStorage.getString(themeKey)
        assertNotNull(savedJson)

        val decodedTheme = Json.decodeFromString<ThemeDto>(savedJson)
        assertEquals(newTheme, decodedTheme)
    }

    @Test
    fun `getTheme with existing storage returns stored theme`() = runTest {
        val storedTheme = ThemeDto.fromColor(
            color = Color(0xFF0000FF),
            paletteStyle = PaletteStyle.Vibrant,
            mode = ThemeMode.SYSTEM
        )
        fakeStorage.putString(themeKey, Json.encodeToString(storedTheme))

        val result = themeRepo.getTheme()

        assertEquals(storedTheme, result)
    }

    @Test
    fun `saveTheme then getTheme returns saved theme`() = runTest {
        val theme = ThemeDto(
            colorValue = Color(0xFF111111).value,
            paletteStyle = SerializablePaletteStyle.NEUTRAL,
            mode = ThemeMode.SYSTEM
        )

        themeRepo.saveTheme(theme)

        val loaded = themeRepo.getTheme()
        assertEquals(theme, loaded)
    }

    @Test
    fun `getTheme reflects latest value stored`() = runTest {
        val firstTheme = ThemeDto(
            colorValue = Color(0xFF111111).value,
            paletteStyle = SerializablePaletteStyle.NEUTRAL,
            mode = ThemeMode.SYSTEM
        )
        val secondTheme = ThemeDto(
            colorValue = Color(0xFF222222).value,
            paletteStyle = SerializablePaletteStyle.TONAL_SPOT,
            mode = ThemeMode.DARK
        )

        themeRepo.saveTheme(firstTheme)
        assertEquals(firstTheme, themeRepo.getTheme())

        themeRepo.saveTheme(secondTheme)
        assertEquals(secondTheme, themeRepo.getTheme())
    }


}