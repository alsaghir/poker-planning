package com.github.alsaghir.pokerplanning.presentation.model

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.DataState
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ThemeViewModel(private val themeRepo: ThemeRepo) : ViewModel() {

    private val _themeState = MutableStateFlow<DataState<ThemeDto>>(DataState.Loading)
    val themeState: StateFlow<DataState<ThemeDto>> = _themeState.asStateFlow()
    private val themeWriteMutex = Mutex()

    // SharedFlow is a hot, configurable replay.
    // You can set replay = 1 to get similar behavior to StateFlow,
    // but it has no concept of "current value" — there is no .value property.
    // It is designed for events (fire and forget), not state (always have a current answer).
    // another option is using limitedParallelism(1) dispatcher
    // https://youtu.be/Ap-cVIM5ORY?t=303
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    val events: SharedFlow<UiEvent> = _events

    init {
        loadTheme()
    }

    private fun loadTheme() {
        _themeState.update { DataState.Loading }
        launchSafe(
            stateFlow = _themeState,
            eventFlow = _events,
            createErrorEvent = { UiEvent.ShowMessage(MessageEvent(it)) },
            errorMessage = "Failed to load theme"
        ) {
            _themeState.update { DataState.Success(themeRepo.getTheme()) }
        }
    }

    fun setTheme(theme: ThemeDto) =
        launchSafe(
            eventFlow = _events,
            createErrorEvent = { UiEvent.ShowMessage(MessageEvent(it)) },
            errorMessage = "Failed to save theme"
        ) {
            themeWriteMutex.withLock {
                Logger.i("Saving theme: $theme")
                themeRepo.saveTheme(theme)
                _themeState.update { DataState.Success(themeRepo.getTheme()) }
            }
        }

}
