package com.github.alsaghir.pokerplanning.presentation.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.SerializablePaletteStyle.EXPRESSIVE
import com.github.alsaghir.pokerplanning.domain.SerializablePaletteStyle.NEUTRAL
import com.github.alsaghir.pokerplanning.domain.Storage
import com.github.alsaghir.pokerplanning.presentation.App
import org.koin.core.Koin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class TopBarITest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `palette selection survives app recreation using real platform storage`() = runComposeUiTest {

        // Given
        val storageKey = "theme"
        lateinit var koinInstance: Koin

        setContent {
            App(onKoinCreated = { koinInstance = it })
        }

        koinInstance.get<Storage>().remove(storageKey)

        // recreate with clean storage
        setContent {
            App(onKoinCreated = { koinInstance = it })
        }
        val storage = koinInstance.get<Storage>()

        Logger.i(storage.getString(storageKey).orEmpty())

        // When
        waitUntil(timeoutMillis = 5_000) {
            runCatching { onNodeWithText(EXPRESSIVE.name).assertExists() }.isSuccess
        }

        // Switch palette from default EXPRESSIVE -> NEUTRAL.
        onNodeWithText(EXPRESSIVE.name).assertExists().performClick()
        onNodeWithText(NEUTRAL.name).assertExists().performClick()
        onNodeWithText(NEUTRAL.name).assertExists()

        // Recreate app
        setContent {
            App()
        }

        waitUntil(timeoutMillis = 5_000) {
            runCatching { onNodeWithText(NEUTRAL.name).assertExists() }.isSuccess
        }


        // Then
        assertFalse(storage.getString(storageKey).isNullOrBlank())
        storage.remove(storageKey)
        assertNull(storage.getString(storageKey))


    }


}