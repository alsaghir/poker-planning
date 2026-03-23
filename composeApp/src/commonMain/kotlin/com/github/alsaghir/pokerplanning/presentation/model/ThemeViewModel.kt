package com.github.alsaghir.pokerplanning.presentation.model

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.github.alsaghir.pokerplanning.domain.DataState
import com.github.alsaghir.pokerplanning.domain.ThemeDto
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(private val themeRepo: ThemeRepo) : ViewModel() {

    val themeState: StateFlow<DataState<ThemeDto>> = themeRepo.themeState

    private val _events = MutableSharedFlow<ThemeUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ThemeUiEvent> = _events

    fun setTheme(theme: ThemeDto) =
        launchSafe(
            eventFlow = _events,
            createErrorEvent = { ThemeUiEvent.ShowMessage(MessageEvent(it)) },
            errorMessage = "Failed to save theme"
        ) {
            themeRepo.saveTheme(theme).getOrThrow()
        }
}


sealed class ThemeUiEvent {

    data class ShowMessage(val event: MessageEvent) : ThemeUiEvent()
}


val LocalThemeViewModel = staticCompositionLocalOf<ThemeViewModel> {
    error("Not provided")
}