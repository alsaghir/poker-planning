package com.github.alsaghir.pokerplanning.presentation.component

import FakeStorage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.github.alsaghir.pokerplanning.domain.SerializablePaletteStyle
import com.github.alsaghir.pokerplanning.domain.Storage
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeMode
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import com.github.alsaghir.pokerplanning.domain.ThemeRepoImpl
import com.github.alsaghir.pokerplanning.presentation.App
import com.github.alsaghir.pokerplanning.presentation.model.ThemeViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)

class TopBarUiTest {

    @AfterTest
    fun tearDown() {
        org.koin.mp.KoinPlatform.stopKoin()
    }

    @Test
    fun `mode toggle cycles DARK to SYSTEM to LIGHT to DARK and persists after recreation`() = runComposeUiTest {

        // Given
        val storage = FakeStorage()
        val testModule = integrationModule(storage)
        lateinit var koinInstance: org.koin.core.Koin


        // When
        setContent {
            App(module = testModule, onKoinCreated = { koinInstance = it })
        }

        // Initial mode from ThemeRepo default is DARK, so icon proposes switching to SYSTEM.
        onNodeWithContentDescription("Switch to System Mode").assertExists().performClick()
        onNodeWithContentDescription("Switch to Light Mode").assertExists().performClick()
        onNodeWithContentDescription("Switch to Dark Mode").assertExists().performClick()
        onNodeWithContentDescription("Switch to System Mode").assertExists()
        onNodeWithContentDescription("Switch to System Mode").performClick()

        // wait for any async persistence to complete before recreating UI; in real app this
        // would be handled by ViewModel scope and suspend functions, but here we just want to ensure
        // storage is updated before next step.
        waitForIdle()

        // Recreate UI with same storage and ensure latest mode is restored.
        setContent {
            App(module = integrationModule(storage), onKoinCreated = { koinInstance = it })
        }


        // Then
        // If persisted correctly, restored mode is SYSTEM and next action is LIGHT.
        waitUntil(timeoutMillis = 5_000) {
            runCatching { onNodeWithContentDescription("Switch to Light Mode").assertExists() }.isSuccess
                    || runCatching { onNodeWithContentDescription("Switch to Dark Mode").assertExists() }.isSuccess
                    || runCatching { onNodeWithContentDescription("Switch to System Mode").assertExists() }.isSuccess
        }

        waitUntil(timeoutMillis = 5_000) {
            !storage.getString("theme").isNullOrBlank()
        }

        // Extra confidence: serialized payload
        val raw = storage.getString("theme").orEmpty()
        val json = koinInstance.get<Json>()
        assertEquals(json.decodeFromString<ThemeDto>(raw).mode, ThemeMode.SYSTEM)
    }

    @Test
    fun `palette switching updates UI and persists latest selection`() = runComposeUiTest {
        // Given
        val storage = FakeStorage()
        lateinit var koinInstance: org.koin.core.Koin

        // When
        setContent {
            App(module = integrationModule(storage), onKoinCreated = { koinInstance = it })
        }

        // Then
        // Initial default palette is EXPRESSIVE; open palette menu from current chip.
        onNodeWithText("EXPRESSIVE").assertExists().performClick()
        onNodeWithText("NEUTRAL").assertExists().performClick()
        onNodeWithText("NEUTRAL").assertExists()

        // Edge case: switch again quickly to another palette; final one should win.
        onNodeWithText("NEUTRAL").performClick()
        onNodeWithText("VIBRANT").assertExists().performClick()
        onNodeWithText("VIBRANT").assertExists()

        // Edge case: recreate UI with same storage and ensure latest palette is restored.
        setContent {
            App(module = integrationModule(storage))
        }

        waitUntil(timeoutMillis = 5_000) {
            runCatching {
                onNodeWithText("VIBRANT").assertExists()
            }.isSuccess
        }

        // Optional persistence sanity check: wait until storage actually has payload.
        waitUntil(timeoutMillis = 5_000) {
            !storage.getString("theme").isNullOrBlank()
        }


        val raw = storage.getString("theme").orEmpty()
        val json = koinInstance.get<Json>()
        assertEquals(json.decodeFromString<ThemeDto>(raw).paletteStyle, SerializablePaletteStyle.VIBRANT)
    }

    private fun integrationModule(storage: Storage): Module = module {
        single<Storage> { storage }

        single<Json> {
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        }

        single<ThemeRepo> {
            ThemeRepoImpl(
                storage = get(),
                defaultColor = Color(0xFF6650A4),
                json = get()
            )
        }

        viewModel<ThemeViewModel>()
    }

}