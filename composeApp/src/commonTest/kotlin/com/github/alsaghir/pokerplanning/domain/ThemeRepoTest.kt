package com.github.alsaghir.pokerplanning.domain

import FakeStorage
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ThemeRepoTest {

    private lateinit var fakeStorage: FakeStorage
    private lateinit var themeRepo: ThemeRepo
    private val defaultColor = Color(0xFFFF0000)

    @BeforeTest
    fun setup() {
        fakeStorage = FakeStorage()
        themeRepo = ThemeRepoImpl(
            storage = fakeStorage,
            defaultColor = defaultColor
        )
    }

    @Test
    fun `initial state should emit default theme`() = runTest {
        themeRepo.themeState.test {
            val initialState = awaitItem()

            assertEquals(defaultColor.value, initialState.colorValue)
            assertEquals(ThemeMode.DARK, initialState.mode)
            assertEquals(SerializablePaletteStyle.EXPRESSIVE, initialState.paletteStyle)

            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `saveTheme should emit new state and persist via Storage interface`() = runTest {
        val newTheme = ThemeDto.fromColor(
            color = Color(0xFF00FF00),
            paletteStyle = PaletteStyle.TonalSpot,
            mode = ThemeMode.LIGHT
        )

        themeRepo.themeState.test {
            // The first emission is always the default state due to StateFlow
            awaitItem()

            // Act: Save a new theme
            themeRepo.saveTheme(newTheme)

            // Assert StateFlow emitted the new theme
            assertEquals(newTheme, awaitItem())

            // Assert storage updated using the proper public interface method
            val savedJson = fakeStorage.getString("theme")
            assertNotNull(savedJson)

            val decodedTheme = Json.decodeFromString<ThemeDto>(savedJson)
            assertEquals(newTheme, decodedTheme)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTheme with existing data should emit stored theme`() = runTest {
        // Arrange
        val storedTheme = ThemeDto.fromColor(
            color = Color(0xFF0000FF),
            paletteStyle = PaletteStyle.Vibrant,
            mode = ThemeMode.SYSTEM
        )
        fakeStorage.putString("theme", Json.encodeToString(storedTheme))

        themeRepo.themeState.test {
            // Skip the initial default state
            awaitItem()

            // Act
            themeRepo.loadTheme()

            // Assert Flow emits the stored state
            assertEquals(storedTheme, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTheme with empty storage should not emit new items`() = runTest {
        // Arrange
        fakeStorage.clear()

        themeRepo.themeState.test {
            val initialState = awaitItem() // Initial default theme

            // Act
            themeRepo.loadTheme()

            // Assert no further state changes occurred because storage is empty
            expectNoEvents()

            // Verify state is still default
            assertEquals(defaultColor.value, initialState.colorValue)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTheme called multiple times emits each persisted value in order`() = runTest {
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

        themeRepo.themeState.test {
            val json = Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
            val defaultTheme = ThemeDto.default(defaultColor)

            assertEquals(defaultTheme, awaitItem())

            fakeStorage.putString("theme", json.encodeToString(firstTheme))
            themeRepo.loadTheme()
            assertEquals(firstTheme, awaitItem())

            fakeStorage.putString("theme", json.encodeToString(secondTheme))
            themeRepo.loadTheme()
            assertEquals(secondTheme, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}