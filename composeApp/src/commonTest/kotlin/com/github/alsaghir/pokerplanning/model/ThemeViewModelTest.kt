// commonTest/kotlin/com/github/alsaghir/pokerplanning/presentation/model/ThemeViewModelTest.kt
package com.github.alsaghir.pokerplanning.presentation.model

import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.github.alsaghir.pokerplanning.domain.DataState
import com.github.alsaghir.pokerplanning.domain.FakeThemeRepo
import com.github.alsaghir.pokerplanning.domain.SerializablePaletteStyle
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepo: FakeThemeRepo
    private lateinit var viewModel: ThemeViewModel

    private val redTheme = ThemeDto(
        colorValue = Color(0xFFFF0000).value,
        paletteStyle = SerializablePaletteStyle.VIBRANT,
        mode = ThemeMode.LIGHT
    )

    private val blueTheme = ThemeDto(
        colorValue = Color(0xFF0000FF).value,
        paletteStyle = SerializablePaletteStyle.NEUTRAL,
        mode = ThemeMode.SYSTEM
    )

    @BeforeTest
    fun setup() {
        // StandardTestDispatcher — coroutines do NOT run until explicitly advanced.
        // This gives us control over when init{} work completes.
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeThemeRepo()
        viewModel = ThemeViewModel(fakeRepo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `themeState emits Loading then Success on init`() = runTest {
        // Create inside runTest — init{} is scheduled but NOT yet executed
        val localViewModel = ThemeViewModel(fakeRepo)

        // Scheduler hasn't ticked yet — still Loading
        assertIs<DataState.Loading>(localViewModel.themeState.value)

        // Now run init{}
        advanceUntilIdle()

        // Now it's Success
        assertIs<DataState.Success<ThemeDto>>(localViewModel.themeState.value)
    }

    @Test
    fun `themeState Success contains repo default theme after init`() = runTest {
        val expectedTheme = fakeRepo.getTheme()

        viewModel.themeState.test {
            val success = awaitItem()
            assertIs<DataState.Success<ThemeDto>>(success)
            assertEquals(expectedTheme, success.data)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `loadTheme failure emits ShowMessage event`() = runTest {
        fakeRepo.getThemeShouldThrow = true
        val failingViewModel = ThemeViewModel(fakeRepo)

        turbineScope {
            // subscribe to BOTH before advancing — no race window
            val stateTurbine = failingViewModel.themeState.testIn(backgroundScope)
            val eventTurbine = failingViewModel.events.testIn(backgroundScope)

            assertIs<DataState.Loading>(stateTurbine.awaitItem())

            advanceUntilIdle() // runs init -> loadTheme -> throws -> launchSafe catches -> emits

            val event = eventTurbine.awaitItem()
            assertIs<UiEvent.ShowMessage>(event)
            assertContains(event.event.message, "Simulated theme loading failure")
            stateTurbine.cancelAndIgnoreRemainingEvents()
            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme updates themeState to new theme`() = runTest {
        viewModel.themeState.test {
            awaitItem()

            viewModel.setTheme(redTheme)

            val updated = awaitItem()
            assertIs<DataState.Success<ThemeDto>>(updated)
            assertEquals(redTheme, updated.data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme called multiple times emits each theme in order`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.themeState.test {
            val initial = awaitItem()
            assertIs<DataState.Success<ThemeDto>>(initial)

            viewModel.setTheme(redTheme)
            testDispatcher.scheduler.advanceUntilIdle()
            val first = awaitItem()
            assertIs<DataState.Success<ThemeDto>>(first)
            assertEquals(redTheme, first.data)

            viewModel.setTheme(blueTheme)
            testDispatcher.scheduler.advanceUntilIdle()
            val second = awaitItem()
            assertIs<DataState.Success<ThemeDto>>(second)
            assertEquals(blueTheme, second.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `setTheme persists theme to repository`() = runTest {
        advanceUntilIdle() // init completes

        viewModel.setTheme(redTheme)
        advanceUntilIdle()

        assertEquals(redTheme, fakeRepo.lastSavedTheme)
    }

    @Test
    fun `setTheme failure emits ShowMessage event without changing state`() = runTest {
        turbineScope {
            val stateTurbine = viewModel.themeState.testIn(backgroundScope)
            val eventTurbine = viewModel.events.testIn(backgroundScope)

            advanceUntilIdle()
            val successItem = stateTurbine.awaitItem()
            assertIs<DataState.Success<ThemeDto>>(successItem)

            fakeRepo.saveThemeShouldThrow = true
            viewModel.setTheme(redTheme)
            advanceUntilIdle()

            val event = eventTurbine.awaitItem()
            assertIs<UiEvent.ShowMessage>(event)
            assertTrue(event.event.message.contains("Simulated saveTheme failure"))

            // state must not have changed after the failed save
            stateTurbine.expectNoEvents()
            assertEquals(successItem, viewModel.themeState.value)

            stateTurbine.cancelAndIgnoreRemainingEvents()
            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themeState replays last value to new subscriber`() = runTest {
        viewModel.themeState.test {
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        // new subscriber after the previous one cancelled
        viewModel.themeState.test {
            // StateFlow always replays the last value immediately
            assertIs<DataState.Success<ThemeDto>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

}