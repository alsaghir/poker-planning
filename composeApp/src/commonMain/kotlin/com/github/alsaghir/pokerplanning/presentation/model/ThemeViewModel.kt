package com.github.alsaghir.pokerplanning.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.alsaghir.pokerplanning.domain.DataState
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ThemeViewModel(private val themeRepo: ThemeRepo) : ViewModel() {

    val themeState: StateFlow<DataState<ThemeDto>> = themeRepo.themeState
        .map { theme -> DataState.Success(theme) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5 * 1000),
            initialValue = DataState.Loading
        )

    private val _events = MutableSharedFlow<ThemeUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ThemeUiEvent> = _events

    init {
        loadTheme()
    }

    private fun loadTheme() {
        launchSafe(
            eventFlow = _events,
            createErrorEvent = { ThemeUiEvent.ShowMessage(MessageEvent(it)) },
            errorMessage = "Failed to load theme"
        ) {
            themeRepo.loadTheme()
        }
    }

    fun setTheme(theme: ThemeDto) =
        launchSafe(
            eventFlow = _events,
            createErrorEvent = { ThemeUiEvent.ShowMessage(MessageEvent(it)) },
            errorMessage = "Failed to save theme"
        ) {
            themeRepo.saveTheme(theme)
        }
}


sealed class ThemeUiEvent {

    data class ShowMessage(val event: MessageEvent) : ThemeUiEvent()
}

