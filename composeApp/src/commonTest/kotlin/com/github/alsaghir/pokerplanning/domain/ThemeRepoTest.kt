package com.github.alsaghir.pokerplanning.domain

import FakeStorage
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun `getTheme with malformed JSON throws serialization exception`() = runTest {
        fakeStorage.putString(themeKey, "{ not-valid-json }")

        assertFailsWith<SerializationException> {
            themeRepo.getTheme()
        }
    }

    @Test
    fun `saveTheme propagates storage exception`() = runTest {
        val failingStorage = object : Storage {
            override fun getString(key: String): String? = null
            override suspend fun putString(key: String, value: String) {
                error("Disk write failed")
            }

            override suspend fun remove(key: String) = Unit
        }

        val repo = ThemeRepoImpl(
            storage = failingStorage,
            defaultColor = defaultColor
        )

        val ex = assertFailsWith<IllegalStateException> {
            repo.saveTheme(ThemeDto.default(defaultColor))
        }

        assertContains(ex.message.orEmpty(), "Disk write failed")
    }

}

class ThemeDtoMappingTest {

    @Test
    fun `fromColor and toColor round trip`() {
        val dto = ThemeDto.fromColor(
            color = Color(0xFF123456),
            paletteStyle = PaletteStyle.Expressive,
            mode = ThemeMode.SYSTEM
        )

        assertEquals(Color(0xFF123456), dto.toColor())
        assertEquals(ThemeMode.SYSTEM, dto.mode)
    }

    @Test
    fun `palette style conversion round trip for supported styles`() {
        val supported = listOf(
            PaletteStyle.TonalSpot,
            PaletteStyle.Neutral,
            PaletteStyle.Vibrant,
            PaletteStyle.Expressive
        )

        supported.forEach { style ->
            val serializable = SerializablePaletteStyle.fromLibraryEnum(style)
            assertEquals(style, serializable.toLibraryEnum())
        }
    }
}
